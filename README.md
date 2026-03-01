# Family App

一个前后端分离的家庭生活小应用，包含：
- 用户登录/建号
- 今日吃什么（随机、地区筛选、转盘过渡）
- 起床签到 / 宝宝签到
- 月历查看签到（支持查看多人签到记录）
- 基于定位的城市美食联网推荐

## 目录结构

- `code/family-app`：Spring Boot 后端
- `frontEnd/family-app`：uni-app + Vue3 前端
- `sql`：数据库变更脚本与说明（后续所有 DB 调整都放这里）

## 当前主要功能

1. **认证与用户**
   - 登录接口：`/api/auth/login`
   - 创建用户接口：`/api/users/createUser`
2. **吃什么**
   - 菜品管理与随机推荐
   - 地区关键词筛选 + 转盘动画过渡
   - “定位并联网搜本地美食”按钮（按经纬度反查城市并推荐）
3. **签到**
   - 起床签到、宝宝签到
   - 月历展示签到标记
   - 可查看某天的签到明细和时间

## 启动方式

## 1) 后端启动（Spring Boot）

前置要求：
- JDK 17
- Maven 3.8+
- MySQL 8+

配置文件：
- `code/family-app/src/main/resources/application.yml`
  - 默认数据库：`jdbc:mysql://localhost:3306/family`
  - 默认账号：`root/root`（建议自行修改）

启动命令：

```bash
cd code/family-app
mvn clean spring-boot:run
```

默认端口：`8080`

## 2) 前端启动（uni-app）

前置要求：
- Node.js 18+（建议）
- npm 9+（建议）

启动命令：

```bash
cd frontEnd/family-app
npm install
npm run dev:h5
```

前端请求基地址：
- 默认在 `frontEnd/family-app/src/utils/request.ts` 中为 `http://localhost:8080/api`
- 也可通过 `VITE_API_BASE_URL` 覆盖

类型检查：

```bash
npm run type-check
```

## 数据库变更规范

- 所有数据库结构/索引/初始化数据调整，统一放到 `sql` 目录。
- 文件名建议：`YYYYMMDD_功能名.sql`
- 每个 SQL 文件建议包含：
  - 变更说明
  - 影响范围
  - 回滚方案（如有）

## 下一步建议

1. **认证安全完善**：将当前登录态升级为真正 JWT/Session 鉴权，并在接口层统一校验。
2. **数据库版本化**：引入 Flyway/Liquibase，规范化执行 `sql` 目录中的脚本。
3. **城市美食能力增强**：加入可配置第三方 API、缓存与限流，提升稳定性和可控性。
4. **测试补齐**：增加后端服务层单测、前端页面关键流程测试。
5. **功能完善**：补全菜品删除/编辑、个人设置等尚未完成功能。

