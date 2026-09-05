# OMES Docker helper — usage: .\docker\omes.ps1 <build|up|down|logs|ps> [args]
param(
    [Parameter(Position = 0)]
    [ValidateSet("build", "up", "down", "logs", "ps")]
    [string]$Command,

    [Parameter(Position = 1)]
    [string]$Target = "all",

    [switch]$SkipMaven,

    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Rest
)

$ErrorActionPreference = "Stop"
if (-not $Command) {
    Write-Host @"
Usage: .\docker\omes.ps1 <build|up|down|logs|ps> [args]
  build [-SkipMaven]     Maven package + image build
  up [infra|all]         Start stack (default all)
  down [app|infra|all]   Stop stack
  logs [service...]      Follow logs
  ps                     Status
"@
    exit 1
}

$Root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $Root

$EnvFile = Join-Path $Root "docker\.env"
$EnvExample = Join-Path $Root "docker\.env.example"
$ComposeFile = Join-Path $Root "docker-compose.yml"

function Ensure-Env {
    if (-not (Test-Path $EnvFile)) {
        Copy-Item $EnvExample $EnvFile
        Write-Host "Created docker/.env from example."
    }
}

function Invoke-Compose {
    param([string[]]$Args)
    Ensure-Env
    & docker compose --env-file $EnvFile -f $ComposeFile @Args
    if ($LASTEXITCODE -ne 0) { throw "docker compose failed ($LASTEXITCODE)" }
}

switch ($Command) {
    "build" {
        Ensure-Env
        foreach ($cfg in @("config\config.properties", "config\era-token.yml")) {
            if (-not (Test-Path (Join-Path $Root $cfg))) {
                throw "Missing $cfg"
            }
        }
        if (-not $SkipMaven) {
            Write-Host "==> mvn package (admin + sas)"
            mvn -pl omes-runner-admin,omes-runner-sas -am clean package -DskipTests
            if ($LASTEXITCODE -ne 0) { throw "Maven failed" }
        }
        $jars = @(
            (Get-ChildItem "omes-runner-admin\target\omes-runner-admin-*.jar" -ErrorAction SilentlyContinue |
                Where-Object { $_.Name -notlike "*.original" } | Select-Object -First 1),
            (Get-ChildItem "omes-runner-sas\target\omes-runner-sas-*.jar" -ErrorAction SilentlyContinue |
                Where-Object { $_.Name -notlike "*.original" } | Select-Object -First 1)
        )
        if ($jars -contains $null) { throw "Fat JARs missing. Run build without -SkipMaven." }
        Write-Host "==> docker build"
        Invoke-Compose --profile app build
        Write-Host "OK. Next: .\docker\omes.ps1 up"
    }
    "up" {
        $mode = if ($Target) { $Target } else { "all" }
        switch ($mode) {
            "infra" { Invoke-Compose up -d postgres redis rabbitmq }
            { $_ -in @("app", "all") } { Invoke-Compose --profile app up -d }
            default { throw "up target: infra | all" }
        }
        Invoke-Compose ps
        Write-Host "Web http://127.0.0.1:8080 | SAS http://127.0.0.1:9400 | Admin :10010 | RabbitMQ :15672"
    }
    "down" {
        $mode = if ($Target) { $Target } else { "all" }
        switch ($mode) {
            "app" {
                Invoke-Compose --profile app stop admin sas web
                Invoke-Compose --profile app rm -f admin sas web
            }
            "infra" {
                Invoke-Compose stop postgres redis rabbitmq
                Invoke-Compose rm -f postgres redis rabbitmq
            }
            "all" { Invoke-Compose --profile app down }
            default { throw "down target: app | infra | all" }
        }
        Write-Host "Stopped ($mode)."
    }
    "logs" {
        $services = @($Target) + @($Rest) | Where-Object { $_ -and $_ -ne "all" }
        if ($services.Count -gt 0) {
            Invoke-Compose --profile app logs -f --tail=200 @services
        } else {
            Invoke-Compose --profile app logs -f --tail=200
        }
    }
    "ps" {
        Invoke-Compose --profile app ps
    }
}
