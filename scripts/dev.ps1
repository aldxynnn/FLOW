$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BackendDir  = Join-Path $ProjectRoot "backend\flow-backend"
$WebDir      = Join-Path $ProjectRoot "web"

Write-Host ""
Write-Host "========================================"
Write-Host " FLOW DEVELOPMENT"
Write-Host "========================================"
Write-Host ""

# 1. PostgreSQL
Write-Host "[1/4] Starting PostgreSQL..."

Push-Location $ProjectRoot

try {
    docker compose up -d postgres
}
finally {
    Pop-Location
}

if ($LASTEXITCODE -ne 0) {
    throw "Docker Compose gagal."
}

Write-Host "PostgreSQL started."
Write-Host ""

# 2. Backend
Write-Host "[2/4] Starting Spring Boot backend..."

$BackendCommand = @"
Set-Location '$BackendDir'
.\gradlew.bat bootRun
"@

Start-Process powershell.exe `
    -ArgumentList "-NoExit", "-Command", $BackendCommand

Write-Host "Backend process started."
Write-Host ""

# 3. Web
Write-Host "[3/4] Starting web server..."

$WebCommand = @"
Set-Location '$WebDir'
python -m http.server 5500
"@

Start-Process powershell.exe `
    -ArgumentList "-NoExit", "-Command", $WebCommand

Write-Host "Web server process started."
Write-Host ""

# 4. Wait for backend
Write-Host "[4/4] Waiting for backend..."

$MaxAttempts = 60
$Attempt = 0

while ($Attempt -lt $MaxAttempts) {
    Start-Sleep -Seconds 1
    $Attempt++

    try {
        $Response = Invoke-WebRequest `
            -Uri "http://localhost:8080/api/health" `
            -UseBasicParsing `
            -TimeoutSec 2

        if ($Response.StatusCode -eq 200) {
            Write-Host ""
            Write-Host "========================================"
            Write-Host " FLOW READY"
            Write-Host "========================================"
            Write-Host ""

            Write-Host "Backend : http://localhost:8080"
            Write-Host "Web     : http://localhost:5500"
            Write-Host ""
            Write-Host "Sekarang tinggal Run Android dari Android Studio."
            Write-Host ""

            exit 0
        }
    }
    catch {
        Write-Host -NoNewline "."
    }
}

Write-Host ""
throw "Backend tidak berhasil ready."