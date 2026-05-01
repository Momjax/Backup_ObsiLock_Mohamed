param (
    [string]$TargetDir = "$HOME\Downloads\coffreFortJava-main"
)

if (!(Test-Path $TargetDir)) { New-Item -ItemType Directory -Path $TargetDir -Force }
Set-Location $TargetDir

# Création des dossiers sources
$dirs = @(
    "src\main\java\com\coffrefort\client\controllers",
    "src\main\java\com\coffrefort\client\util",
    "src\main\java\com\coffrefort\client\model",
    "src\main\java\com\coffrefort\client\config",
    "src\main\resources\com\coffrefort\client"
)
foreach ($dir in $dirs) { New-Item -ItemType Directory -Path $dir -Force }

# On va supposer que tu as déjà les fichiers de base sur ton serveur, 
# mais je vais te donner les 3 fichiers CRITIQUES corrigés à mettre dedans.

