# OMES Docker — usage: .\docker.ps1 <build|up|down> [infra]
param(
    [Parameter(Position = 0)]
    [ValidateSet("build", "up", "down")]
    [string]$Command,

    [Parameter(Position = 1)]
    [ValidateSet("infra", "all")]
    [string]$Target = "all",

    [switch]$SkipMaven,
    [switch]$SkipWeb
)

$ErrorActionPreference = "Stop"
if (-not $Command) {
    Write-Host @"
Usage: .\docker.ps1 <build|up|down> [infra]
  build [-SkipMaven] [-SkipWeb]   package jars + vite dist + images
  up [infra|all]                  start (default all = infra + app)
  down                            stop & remove app stack
"@
    exit 1
}

$Root = $PSScriptRoot
Set-Location $Root
$WebRoot = Join-Path (Split-Path $Root -Parent) "omes-web-admin"

if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
    Write-Host "Created .env from .env.example"
}

function Invoke-Compose([string[]]$Args) {
    & docker compose @Args
    if ($LASTEXITCODE -ne 0) { throw "docker compose failed ($LASTEXITCODE)" }
}

switch ($Command) {
    "build" {
        foreach ($f in @("config\config.properties", "config\era-token.yml")) {
            if (-not (Test-Path $f)) { throw "Missing $f" }
        }
        if (-not $SkipMaven) {
            Write-Host "==> mvn package"
            mvn -pl omes-runner-admin,omes-runner-sas -am clean package -DskipTests
            if ($LASTEXITCODE -ne 0) { throw "Maven failed" }
        }
        $ok = @(
            (Get-ChildItem "omes-runner-admin\target\omes-runner-admin-*.jar" -EA SilentlyContinue | Where-Object Name -notlike "*.original"),
            (Get-ChildItem "omes-runner-sas\target\omes-runner-sas-*.jar" -EA SilentlyContinue | Where-Object Name -notlike "*.original")
        ) | Where-Object { $_ }
        if ($ok.Count -lt 2) { throw "Fat JARs missing" }

        if (-not $SkipWeb) {
            if (-not (Test-Path $WebRoot)) { throw "Missing $WebRoot" }
            Write-Host "==> vite build → dist"
            Push-Location $WebRoot
            try {
                if (-not (Test-Path node_modules)) { npm install --no-audit --no-fund }
                $env:VITE_SAS_BASE_URL = ""
                npx vite build
                if ($LASTEXITCODE -ne 0) { throw "vite build failed" }
            } finally { Pop-Location }
        }
        if (-not (Test-Path "$WebRoot\dist\index.html")) { throw "Missing frontend dist" }

        Write-Host "==> docker compose build"
        Invoke-Compose --profile app build
        Write-Host "OK → .\docker.ps1 up"
    }
    "up" {
        if ($Target -eq "infra") {
            Invoke-Compose up -d postgres redis rabbitmq
        } else {
            Invoke-Compose --profile app up -d
        }
        Invoke-Compose ps
        Write-Host "Web :8080 | SAS :9400 | Admin :10010 | RabbitMQ mgmt :15672"
    }
    "down" {
        Invoke-Compose --profile app down
        Write-Host "Stopped."
    }
}
