$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$required = @(
    'README.md',
    'RUN-FIRST.md',
    'docker-compose.yml',
    'android\gradlew.bat',
    'android\gradle\wrapper\gradle-wrapper.jar',
    'android\gradle\wrapper\gradle-wrapper.properties',
    'android\settings.gradle.kts',
    'android\app\build.gradle.kts',
    'backend\flow-backend\gradlew.bat',
    'backend\flow-backend\build.gradle.kts',
    'backend\flow-backend\src\main\resources\application.properties',
    'backend\flow-backend\src\main\kotlin\com\aldxynnn\flowbackend\FlowBackendApplication.kt',
    'backend\flow-backend\src\main\kotlin\com\aldxynnn\flowbackend\health\HealthController.kt',
    'web\index.html',
    'web\app.js',
    'web\styles.css'
)
foreach ($item in $required) {
    $path = Join-Path $root $item
    if (-not (Test-Path $path)) { throw "Missing required file: $item" }
}
Write-Host 'FLOW structure check: OK'
Write-Host 'Android Gradle wrapper files: OK'
Write-Host 'Backend Gradle wrapper files: OK'
if (Get-Command node -ErrorAction SilentlyContinue) {
    Push-Location (Join-Path $root 'web')
    node --check app.js
    Pop-Location
    Write-Host 'Web JavaScript syntax: OK'
} else {
    Write-Host 'Node not installed: JavaScript syntax check skipped.'
}
Write-Host ''
Write-Host 'Full Gradle builds must be run on a machine with the required JDK, Android SDK, and network/dependency access.'
