param (
    $TargetDir = "$HOME\Downloads\coffreFortJava-main"
)

if (!(Test-Path $TargetDir)) { New-Item -ItemType Directory -Path $TargetDir -Force }
Set-Location $TargetDir

$baseUrl = "http://51.75.204.144/source/" # Dossier temporaire sur le serveur

function DownloadFile($path) {
    $dest = Join-Path $TargetDir $path.Replace("/", "\")
    $dir = Split-Path $dest
    if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force }
    $url = $baseUrl + $path
    Write-Host "Downloading $path..."
    Invoke-WebRequest -Uri $url -OutFile $dest
}

# Liste des fichiers à télécharger
$files = @(
    "pom.xml",
    "src/main/java/com/coffrefort/client/App.java",
    "src/main/java/com/coffrefort/client/ApiClient.java",
    "src/main/java/com/coffrefort/client/controllers/MainController.java",
    "src/main/java/com/coffrefort/client/util/JsonUtils.java",
    "src/main/java/com/coffrefort/client/util/style-javafx.css"
)

foreach ($f in $files) { DownloadFile $f }

Write-Host "Compilation..."
$env:JAVA_HOME = "C:\Users\M0mjax\.jdks\ms-17.0.18"
& "C:\Users\M0mjax\AppData\Local\Programs\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin\mvn.cmd" clean compile javafx:run
