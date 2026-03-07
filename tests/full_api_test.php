<?php
// Test script for ObsiLock API
// Running from INSIDE the container or where PHP is available (CLI)

$baseUrl = 'http://localhost:80'; // Inside container, port 80 is direct
if (getenv('BASE_URL')) {
    $baseUrl = getenv('BASE_URL');
}

echo "Testing API at: $baseUrl\n";

// Helper function for cURL requests
function apiRequest($method, $endpoint, $data = [], $token = null, $isFileUpload = false) {
    global $baseUrl;
    $url = $baseUrl . $endpoint;
    $ch = curl_init($url);
    
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);
    
    $headers = [];
    if ($token) {
        $headers[] = 'Authorization: Bearer ' . $token;
    }
    
    if ($isFileUpload) {
        // Multipart/form-data is handled automatically by cURL when passing array with CURLFile
        curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
    } elseif (!empty($data)) {
        $jsonData = json_encode($data);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $jsonData);
        $headers[] = 'Content-Type: application/json';
    }
    
    if (!empty($headers)) {
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    }
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    
    if (curl_errno($ch)) {
        echo "cURL Error: " . curl_error($ch) . "\n";
    }
    
    curl_close($ch);
    
    return ['code' => $httpCode, 'body' => $response];
}

// 1. REGISTER / LOGIN
echo "\n--- 1. Authentication ---\n";
$email = 'test_' . time() . '@example.com';
$password = 'password123';

echo "Registering user: $email\n";
$res = apiRequest('POST', '/auth/register', ['email' => $email, 'password' => $password, 'name' => 'Tester']);
echo "Register Status: " . $res['code'] . "\n";

echo "Logging in...\n";
$res = apiRequest('POST', '/auth/login', ['email' => $email, 'password' => $password]);
$loginData = json_decode($res['body'], true);
$token = $loginData['token'] ?? null;

if (!$token) {
    die("Login failed! Response: " . $res['body']);
}
echo "Login Status: " . $res['code'] . " (Token received)\n";

// 2. UPLOAD FILE (V1)
echo "\n--- 2. Upload File (V1) ---\n";
$filename = 'test_v1.txt';
file_put_contents($filename, 'This is version 1 content ' . time());
$cFile = new CURLFile(realpath($filename), 'text/plain', $filename);

$res = apiRequest('POST', '/files', ['file' => $cFile], $token, true);
echo "Upload V1 Status: " . $res['code'] . "\n";
$uploadData = json_decode($res['body'], true);
$fileId = $uploadData['id'] ?? null;

if (!$fileId) {
    die("Upload failed! Response: " . $res['body']);
}
echo "File uploaded with ID: $fileId\n";

// 3. UPLOAD VERSION (V2)
echo "\n--- 3. Upload Version (V2) ---\n";
$filenameV2 = 'test_v2.txt';
file_put_contents($filenameV2, 'This is version 2 content (ENCRYPTED CHECK) ' . time());
$cFileV2 = new CURLFile(realpath($filenameV2), 'text/plain', $filenameV2);

$res = apiRequest('POST', "/files/$fileId/versions", ['file' => $cFileV2], $token, true);
echo "Upload V2 Status: " . $res['code'] . "\n";
$v2Data = json_decode($res['body'], true);
$versionId = $v2Data['version']['id'] ?? null; // Adjust according to response structure
// If response structure is different, check raw body

echo "Response Body: " . substr($res['body'], 0, 100) . "...\n";

// 4. DOWNLOAD V2 (Verify Decryption)
echo "\n--- 4. Download V2 (Decryption Test) ---\n";
// The ID of the version 2 is usually 2 if sequential, or we get it from listVersions
// Let's assume version number is 2
$res = apiRequest('GET', "/files/$fileId/versions/2/download", [], $token);
echo "Download V2 Status: " . $res['code'] . "\n";
$downloadedContent = $res['body'];
$originalContent = file_get_contents($filenameV2);

if ($downloadedContent === $originalContent) {
    echo "SUCCESS: V2 Content matches original (Decryption works!)\n";
} else {
    echo "FAILURE: V2 Content mismatch!\n";
    echo "Expected: $originalContent\n";
    echo "Got: " . substr($downloadedContent, 0, 100) . "...\n";
}

// 5. CREATE FOLDER & SHARE (ZIP Test)
echo "\n--- 5. Share Folder (ZIP Test) ---\n";
// Create folder
$res = apiRequest('POST', '/folders', ['name' => 'ZipTestFolder'], $token);
$folderData = json_decode($res['body'], true);
$folderId = $folderData['id'] ?? null;
echo "Folder Created ID: $folderId\n";

// Upload file to folder directly to simplify (or move, but upload is easier to test)
$filenameInFolder = 'file_in_folder.txt';
file_put_contents($filenameInFolder, 'Content inside folder zip test');
$cFileInFolder = new CURLFile(realpath($filenameInFolder), 'text/plain', $filenameInFolder);
// Passing folder_id in multpart/form-data
$res = apiRequest('POST', '/files', ['file' => $cFileInFolder, 'folder_id' => $folderId], $token, true);
echo "File in Folder Upload Status: " . $res['code'] . "\n";

// Share Folder
$res = apiRequest('POST', '/shares', ['kind' => 'folder', 'target_id' => $folderId], $token);
$shareData = json_decode($res['body'], true);
$shareToken = $shareData['token'] ?? null;
echo "Share Created Token: $shareToken\n";

// Download ZIP (Public)
if ($shareToken) {
    echo "Downloading ZIP from /s/$shareToken/download ...\n";
    $res = apiRequest('POST', "/s/$shareToken/download", [], null); // Public route
    echo "ZIP Download Status: " . $res['code'] . "\n";
    
    // Check if it looks like a zip (PK header)
    if (strpos($res['body'], 'PK') === 0) {
        echo "SUCCESS: Response looks like a ZIP file (starts with PK)\n";
        file_put_contents('test_download.zip', $res['body']);
        echo "Saved to test_download.zip\n";
    } else {
        echo "FAILURE: Response does not look like a ZIP.\n";
        echo "Body snippet: " . substr($res['body'], 0, 100) . "\n";
    }
}

// 6. ACTIVITY LOG
echo "\n--- 6. Activity Log ---\n";
$res = apiRequest('GET', '/me/activity', [], $token);
echo "Activity Status: " . $res['code'] . "\n";
$activityData = json_decode($res['body'], true);

if (isset($activityData['data']) && count($activityData['data']) > 0) {
    echo "SUCCESS: Activity log retrieved. Count: " . count($activityData['data']) . "\n";
    print_r($activityData['data'][0]); // Print first item
} else {
    echo "WARNING: Activity log empty or invalid format.\n";
    echo "Body: " . $res['body'] . "\n";
}

// Cleanup
@unlink($filename);
@unlink($filenameV2);
@unlink($filenameInFolder);
@unlink('test_download.zip');

echo "\n--- End of Tests ---\n";
