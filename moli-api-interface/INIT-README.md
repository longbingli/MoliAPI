# 数据库初始化说明

## 问题原因

应用启动成功，但返回500错误，原因是：**数据库表不存在**

错误信息：`Table 'moliapi_interface.poetry' doesn't exist`

## 解决方案

### 方法一：使用MySQL命令行手动初始化（推荐）

1. 打开MySQL命令行客户端
2. 执行以下命令：

```bash
mysql -u root -p
```

3. 输入密码后，执行SQL文件：

```sql
SOURCE e:/MoliAPI/moli-api-interface/init-db.sql;
```

### 方法二：使用MySQL客户端工具

使用Navicat、DBeaver、MySQL Workbench等工具：

1. 连接到MySQL数据库
2. 打开SQL文件：`e:/MoliAPI/moli-api-interface/init-db.sql`
3. 执行整个文件

### 方法三：使用PowerShell（如果MySQL已添加到PATH）

```powershell
mysql -u root -p1314 < e:\MoliAPI\moli-api-interface\init-db.sql
```

## 验证初始化

执行完初始化后，验证表是否创建成功：

```sql
USE moliapi_interface;
SHOW TABLES;
SELECT * FROM poetry;
```

## 重启应用

数据库初始化完成后，重启应用：

```bash
mvn spring-boot:run
```

## 测试接口

```bash
curl http://localhost:8080/api/poetry/1
```

预期响应：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "title": "静夜思",
    "author": "李白",
    "dynasty": "唐",
    "content": "床前明月光，疑是地上霜。举头望明月，低头思故乡。",
    "tags": "思乡,月亮"
  }
}
```

## 常见问题

### 1. MySQL服务未启动

Windows系统：
```cmd
net start mysql
```

### 2. 权限不足

确保MySQL用户有创建数据库和表的权限。

### 3. 数据库已存在

如果数据库已存在，会显示警告，可以忽略或使用：

```sql
DROP DATABASE IF EXISTS moliapi_interface;
```

然后重新执行初始化脚本。
