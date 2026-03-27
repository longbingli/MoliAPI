# API测试脚本

Write-Host "=== 古诗词查询接口测试 ===" -ForegroundColor Green

# 测试1: 根据ID查询
Write-Host "`n1. 测试根据ID查询诗词..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/poetry/1" -Method Get
    Write-Host "成功！返回数据:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "失败: $_" -ForegroundColor Red
}

# 测试2: 根据标题查询
Write-Host "`n2. 测试根据标题查询诗词..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/poetry/title?title=静夜思" -Method Get
    Write-Host "成功！返回数据:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "失败: $_" -ForegroundColor Red
}

# 测试3: 根据作者查询
Write-Host "`n3. 测试根据作者查询诗词..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/poetry/author?author=李白" -Method Get
    Write-Host "成功！返回数据:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "失败: $_" -ForegroundColor Red
}

# 测试4: 根据朝代查询
Write-Host "`n4. 测试根据朝代查询诗词..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/poetry/dynasty?dynasty=唐" -Method Get
    Write-Host "成功！返回数据:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "失败: $_" -ForegroundColor Red
}

# 测试5: 关键词搜索
Write-Host "`n5. 测试关键词搜索..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/poetry/keyword?keyword=李白" -Method Get
    Write-Host "成功！返回数据:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "失败: $_" -ForegroundColor Red
}

# 测试6: 高级搜索（分页）
Write-Host "`n6. 测试高级搜索（分页）..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/poetry/search?dynasty=唐&current=1&pageSize=5" -Method Get
    Write-Host "成功！返回数据:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "失败: $_" -ForegroundColor Red
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
