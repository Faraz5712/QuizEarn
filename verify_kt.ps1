$files = Get-ChildItem -Path "app\src" -Filter "*.kt" -Recurse
$errCount = 0
foreach ($f in $files) {
    $content = Get-Content $f.FullName -Raw
    $open = ([regex]::Matches($content, '\{')).Count
    $close = ([regex]::Matches($content, '\}')).Count
    if ($open -ne $close) {
        Write-Host "Mismatch in $($f.Name): open=$open close=$close"
        $errCount++
    }
}
Write-Host "Checked $($files.Count) files. Mismatches: $errCount"
