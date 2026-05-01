import os

def get_content(path):
    with open(path, 'r', encoding='utf-8') as f:
        return f.read().replace('`', '``').replace('$', '`$')

files_to_include = [
    "pom.xml",
    "src/main/java/com/coffrefort/client/App.java",
    "src/main/java/com/coffrefort/client/ApiClient.java",
    "src/main/java/com/coffrefort/client/util/JsonUtils.java",
    "src/main/java/com/coffrefort/client/controllers/MainController.java",
    "src/main/java/com/coffrefort/client/controllers/FileDetailsController.java",
]

print("$target = \"$HOME\\Downloads\\coffreFortJava-main\"; if (!(Test-Path $target)) { New-Item -ItemType Directory -Path $target -Force }; cd $target;")
for f_path in files_to_include:
    full_path = os.path.join("coffreFortJava-main", f_path)
    if os.path.exists(full_path):
        content = get_content(full_path)
        ps_path = f_path.replace("/", "\\")
        print(f'$content = @"\n{content}\n"@; $dir = Split-Path "{ps_path}"; if (!(Test-Path $dir)) {{ New-Item -ItemType Directory -Path $dir -Force }}; [System.IO.File]::WriteAllText("{ps_path}", $content, [System.Text.Encoding]::ASCII)')

print('$env:JAVA_HOME = "C:\\Users\\M0mjax\\.jdks\\ms-17.0.18"; & "C:\\Users\\M0mjax\\AppData\\Local\\Programs\\IntelliJ IDEA 2025.3.3\\plugins\\maven\\lib\\maven3\\bin\\mvn.cmd" clean compile javafx:run')
