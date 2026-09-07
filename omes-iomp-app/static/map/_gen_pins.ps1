$ErrorActionPreference = "Stop"
$dir = Join-Path $PSScriptRoot "."
if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
Add-Type -AssemblyName System.Drawing

function Save-Pin {
    param([string]$Path, [System.Drawing.Color]$Fill)
    $w = 48
    $h = 56
    $bmp = New-Object System.Drawing.Bitmap $w, $h
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = "AntiAlias"
    $g.Clear([System.Drawing.Color]::Transparent)
    $brush = New-Object System.Drawing.SolidBrush $Fill
    $pen = New-Object System.Drawing.Pen -ArgumentList @([System.Drawing.Color]::FromArgb(140, 0, 0, 0), 2)
    $cx = $w / 2.0
    $r = 14.0
    $pathObj = New-Object System.Drawing.Drawing2D.GraphicsPath
    $pathObj.AddArc([float]($cx - $r), 8.0, [float]($r * 2), [float]($r * 2), 0, 360)
    $pathObj.AddLine([float]($cx - $r * 0.88), [float](8 + $r * 1.55), [float]$cx, [float]($h - 6))
    $pathObj.AddLine([float]$cx, [float]($h - 6), [float]($cx + $r * 0.88), [float](8 + $r * 1.55))
    $pathObj.CloseFigure()
    $g.FillPath($brush, $pathObj)
    $g.DrawPath($pen, $pathObj)
    $bmp.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
    $pathObj.Dispose()
    $brush.Dispose()
    $pen.Dispose()
}

Save-Pin (Join-Path $dir "pin_online.png") ([System.Drawing.Color]::FromArgb(255, 0, 216, 255))
Save-Pin (Join-Path $dir "pin_offline.png") ([System.Drawing.Color]::FromArgb(255, 138, 147, 161))
Save-Pin (Join-Path $dir "pin_alarm.png") ([System.Drawing.Color]::FromArgb(255, 255, 90, 90))
Save-Pin (Join-Path $dir "pin_running.png") ([System.Drawing.Color]::FromArgb(255, 65, 213, 122))
Save-Pin (Join-Path $dir "pin_stopped.png") ([System.Drawing.Color]::FromArgb(255, 255, 204, 0))
Write-Host "pins ok"
