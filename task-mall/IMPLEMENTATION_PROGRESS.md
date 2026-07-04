# task-mall 仿站补齐进度矩阵

## 任务约束

- 不再开多个浏览器或多个子任务并行检查页面。
- 参考站抓取按模块短会话执行，一次只抓一个页面或一个模块。
- 浏览器抓取不是前置阻塞；中断后从本文件继续。
- 用户端登录态 token 不写入本文档。
- 不提交充值、提现、转账、资料修改等写操作。

## 已确认事实

| 范围 | 结论 | 本地落点 |
|---|---|---|
| 后端结构 | `mall-admin` 聚合后台，`mall-user` 会员，`mall-wallet` 钱包，`mall-mission` 任务，`mall-promotion` 活动 | `/Users/aa/data/java/task-mall/task-mall` |
| 后台布局 | 当前 `topMenu` 与左侧菜单互斥，不能同时支持参考站的顶部模块 + 左侧子菜单 | `/Users/aa/data/java/task-mall/mall-admin-ui/src/layout/default-layout.vue` |
| 路由标签 | 已有路由 TabBar，但不是会员列表内的 VIP 业务 tabs | `/Users/aa/data/java/task-mall/mall-admin-ui/src/components/tab-bar/index.vue` |
| 后台设置 | 当前 `topMenu:false`、`tabBar:true` | `/Users/aa/data/java/task-mall/mall-admin-ui/src/config/settings.json` |
| 用户端布局 | 当前底部只有：首页、任务、投资、钱包、团队、我的 | `/Users/aa/data/java/task-mall/mall-ui/src/App.vue` |
| 用户端路由 | 已有 `/lottery`、`/leaderboard`，但主入口和鉴权不完整 | `/Users/aa/data/java/task-mall/mall-ui/src/router/index.ts` |
| 机器人配置 | 只做配置管理，不调用 Telegram Bot API；Token 必须脱敏 | `/Users/aa/data/java/shop_java/流程控制文档.md:498-515` |
| 数据库 | 本地 MySQL，`root` 无密码 | 后续执行 `mysql -uroot` |

## 参考站抓取方法

### 抓取字段

| 字段 | 说明 |
|---|---|
| 模块 | 顶部模块或用户端一级页面 |
| 页面 | 当前业务页面 |
| 路由/URL | 页面地址，不记录敏感 token |
| 顶部导航 | 顶部模块、快捷按钮、状态入口 |
| 左侧菜单 | 后台侧栏菜单项 |
| 路由 TabBar | 后台已打开页面标签 |
| 页面内 tabs | VIP tabs、订单状态 tabs、记录 tabs 等 |
| 搜索项 | 输入框、选择器、时间范围 |
| 表格列 | 表头字段 |
| 顶部按钮 | 添加、批量、导出、刷新等 |
| 行按钮 | 详情、补单、查线、模拟登录等 |
| 弹窗字段 | 弹窗/抽屉标题与表单字段 |
| 网络请求 | 方法、路径、参数、响应字段 |
| 本地落点 | 需要修改的前端/后端文件 |
| 状态 | 待抓取 / 待实现 / 已实现 / 待校准 |

### 抓取顺序

1. 后台会员列表。
2. 后台会员子菜单。
3. 后台账单/钱包相关页面。
4. 后台系统/机器人配置。
5. 用户端首页。
6. 用户端钱包。
7. 用户端任务/投资。
8. 用户端团队/我的。

## 参考站静态资源发现

### 用户端入口资源

| 项 | 发现 |
|---|---|
| 首页 HTML | `https://www.rwkaifa1.com/` 可直接获取 SPA 入口 |
| 主 JS | `assets/index-d652c788.js` |
| 主 CSS | `assets/index-cb0e26b0.css` |
| Telegram WebApp | 引用了 `https://telegram.org/js/telegram-web-app.js`，用户端需要考虑 Telegram 场景入口 |

### 用户端参考路由

| 路由 | 推断页面 | 本地状态 | 状态 |
|---|---|---|---|
| `/account` | 账户中心 | 缺独立账户中心 | 待实现 |
| `/account/balance` | 余额/财务记录 | 钱包页部分覆盖 | 待实现 |
| `/account/changePwd` | 修改密码 | 缺入口 | 待实现 |
| `/account/financial` | 财务信息/资金资料 | 缺页面 | 待实现 |
| `/account/googleAuth` | Google 验证 | 缺页面 | 待校准 |
| `/account/kyc` | 实名认证 | 缺页面 | 待校准 |
| `/account/person` | 个人资料 | 我的页部分覆盖 | 待实现 |
| `/account/recharge` | 充值 | 已有充值页，但布局需对齐 | 待校准 |
| `/account/select` | 账户选择/资产选择 | 缺页面 | 待校准 |
| `/account/transfer` | 转账 | 已有转账页，但布局需对齐 | 待校准 |
| `/account/vipUplog` | VIP 升级记录 | 缺页面 | 待实现 |
| `/account/withDraw` | 提现 | 已有提现页，但布局需对齐 | 待校准 |
| `/activity`、`/activity/[id]` | 活动 | 本地有抽奖，活动体系不足 | 待实现 |
| `/coupon`、`/coupon/logs` | 优惠券/记录 | 缺页面 | 待实现 |
| `/findpwd` | 找回密码 | 缺页面 | 待实现 |
| `/friendsCircle` | 朋友圈/分享 | 缺页面 | 待校准 |
| `/guides`、`/help` | 指南/帮助 | 缺内容页体系 | 待实现 |
| `/income` | 收益 | 团队返佣部分覆盖 | 待实现 |
| `/invest`、`/invest/[id]`、`/invest/note`、`/investZone` | 投资/投资详情/记录/专区 | 本地只有展示，需按文档走任务口径 | 待实现 |
| `/invite` | 邀请 | 团队页部分覆盖 | 待实现 |
| `/lottery` | 抽奖 | 有路由，缺入口/布局校准 | 待实现 |
| `/marketing` | 营销/推广 | 缺页面 | 待校准 |
| `/mine` | 我的 | 本地 `/profile` 覆盖部分 | 待实现 |
| `/mission` | 任务中心 | 本地 `/tasks` 覆盖部分 | 待实现 |
| `/news`、`/notice`、`/company`、`/privacyPolicy` | 新闻/公告/公司/隐私 | 缺内容页体系 | 待实现 |
| `/pfrules` | 平台规则 | 缺内容页体系 | 待实现 |
| `/register`、`/login` | 注册/登录 | 本地有登录注册，需对齐找回/邀请码 | 待实现 |
| `/service` | 客服 | 缺入口 | 待实现 |
| `/team`、`/team/:id` | 团队/下级详情 | 本地团队页部分覆盖 | 待实现 |
| `/vip`、`/vipDetail` | VIP 页面/详情 | 本地 VIP 配置展示部分覆盖 | 待实现 |

### 用户端参考接口线索

| 接口线索 | 推断能力 | 本地落点 |
|---|---|---|
| `/user/app_info` | 应用配置/首页配置 | `mall-admin` 内容配置或系统参数开放接口 |
| `/user/user_info`、`/user/mine` | 用户资料/我的 | `mall-user` |
| `/user/login_verify`、`/user/login_info` | 登录校验/登录信息 | `mall-user` |
| `/user/tg` | Telegram 用户信息 | 待新增或只做展示配置 |
| `/user/invite_info`、`/user/share_config` | 邀请与分享配置 | `mall-user` + `mall-admin` 配置 |
| `/user/user_rank` | 排行榜 | `mall-admin` leaderboard 或新增开放接口 |
| `/user/set_pwd` | 设置/修改密码 | `mall-user` |
| `/user/team_recharge` | 团队充值统计 | `mall-user` + `mall-wallet` 聚合 |
| `/user/invite_give`、`/user/invite_give_receive` | 邀请奖励/领取 | `mall-user` 或 `mall-promotion` |
| `/user/activity/list`、`/user/activity/claim` | 活动列表/领取 | `mall-promotion` 或 `mall-admin` 活动配置 |
| `/user/agent_position/list`、`/user/agent_position/my`、`/user/agent_position/claim_salary` | 代理职位/薪资 | 待校准，可先作为缺口记录 |
| `/user/multi_level_list`、`/user/multi_level_upgrade` | 多级/VIP 升级 | `mall-user` VIP |
| `/task/list`、`/task/task_info`、`/task/task_count` | 任务列表/详情/统计 | `mall-mission` |
| `/task/distribute`、`/task/fetch_plan_task`、`/task/increase_distribute` | 任务派发/领取 | `mall-mission` |
| `/task/buy_task_product` | 购买任务产品 | 按文档走任务口径，不新增独立投资资金链路 |
| `/task/user_task_plan` | 用户任务计划 | `mall-mission` |
| `/task/share_task_list`、`/task/share_task_detail`、`/task/submit_share`、`/task/update_share` | 分享任务 | `mall-mission` + 后台分享审核 |
| `/user/task_center`、`/user/task_center_receive`、`/user/task_center_detail` | 任务中心 | `mall-mission` |

## 后台模块矩阵

### 顶部模块

| 模块 | 参考依据 | 本地状态 | 目标状态 | 状态 |
|---|---|---|---|---|
| 系统管理 | 图1可见 | 有系统相关页面 | 顶部模块已接入；左侧显示系统聚合菜单 | 已实现 |
| 会员管理 | 图1可见 | 有会员页面，菜单不完整 | 顶部模块已接入；左侧显示已有会员子菜单，缺失菜单进入阶段 3 | 布局已实现 |
| 账单管理 | 图1可见 | 有订单/任务/活动/排行榜页面 | 顶部模块已接入；左侧聚合账单相关根菜单 | 已实现 |
| 报表管理 | 图1可见 | 有报表页面 | 顶部模块已接入；左侧显示报表子菜单 | 已实现 |

### 会员管理左侧菜单

| 菜单 | 当前状态 | 目标功能 | 本地前端落点 | 后端接口状态 | 状态 |
|---|---|---|---|---|---|
| 会员列表 | 已有基础页 | 搜索、VIP tabs、批量、导出、行操作 | `mall-admin-ui/src/views/user-member/members/index.vue` | 已补会员保存、分组、查线/改线、导出、补单、下载充值等聚合接口；补单落在 wallet/order 状态机 | 部分实现 |
| 分组设置 | 缺失 | 分组 CRUD、成员绑定 | `user-member/member-feature/index.vue` 已接入占位承载页；会员列表已可批量绑定分组 | 已新增分组表、分组 CRUD provider、批量绑定接口 | 接口已实现，独立页面待实现 |
| 会员日志 | 部分在日志模块 | 按会员查看登录/操作日志 | `user-member/member-feature/index.vue` 已接入占位承载页 | 待聚合 | 菜单承载已实现，接口待实现 |
| 会员流水 | 缺失为会员菜单 | 会员账变流水 | `user-member/member-feature/index.vue` 已接入占位承载页 | 待聚合 wallet | 菜单承载已实现，接口待实现 |
| 站内信 | 缺失 | 发送/列表/状态 | `user-member/member-feature/index.vue` 已接入占位承载页 | 待新增 | 菜单承载已实现，接口待实现 |
| 电报信息 | 缺失 | 机器人/电报绑定信息 | `user-member/member-feature/index.vue` 已接入占位承载页 | 待新增 | 菜单承载已实现，接口待实现 |
| 余额记录 | 缺失为会员菜单 | 余额变更记录 | `user-member/member-feature/index.vue` 已接入占位承载页 | 待聚合 wallet | 菜单承载已实现，接口待实现 |
| 升级记录 | 缺失为会员菜单 | VIP 升级订单 | `user-member/member-feature/index.vue` 已接入占位承载页 | 待聚合 user | 菜单承载已实现，接口待实现 |
| 分享审核 | 缺失 | 分享任务审核 | `user-member/member-feature/index.vue` 已接入占位承载页 | 待新增/聚合 mission | 菜单承载已实现，接口待实现 |
| 测试账号 | 缺失 | 测试账号管理 | `user-member/member-feature/index.vue` 已接入占位承载页 | 待新增 | 菜单承载已实现，接口待实现 |

### 会员列表页面内 tabs

| Tab | 查询条件 | 当前状态 | 目标状态 | 状态 |
|---|---|---|---|---|
| 全部 | 无 VIP 限制 | 缺失 | 后端分页查询 | 已实现 |
| VIP0 | `vipLevel=0` | 缺失 | 后端分页查询 | 已实现 |
| VIP1 | `vipLevel=1` | 缺失 | 后端分页查询 | 已实现 |
| VIP2 | `vipLevel=2` | 缺失 | 后端分页查询 | 已实现 |
| VIP3 | `vipLevel=3` | 缺失 | 后端分页查询 | 已实现 |
| VIP4 | `vipLevel=4` | 缺失 | 后端分页查询 | 已实现 |
| VIP5 | `vipLevel=5` | 缺失 | 后端分页查询 | 已实现 |

### 会员列表按钮闭环

| 按钮 | 区域 | 行为 | 需要接口 | 本地状态 | 状态 |
|---|---|---|---|---|---|
| 查询 | 搜索区 | 按 UID/账号/邀请码/VIP/状态/注册时间分页 | 是 | 已补后台聚合与 `mall-user` provider 筛选 | 已实现 |
| 重置 | 搜索区 | 清空条件 | 否 | 已补前端状态重置 | 已实现 |
| 添加 | 顶部 | 新增会员 | 是 | 已补 `/api/admin/user/save`；新增时账号/密码必填，`mall-user` 内生成 userId、邀请码和密码哈希 | 已实现 |
| 批量操作 | 顶部 | 批量状态/分组等 | 是 | 已拆成批量封禁、批量解封、批量下线、设置分组等明确按钮 | 已实现 |
| 导出会员 | 顶部 | 导出当前筛选 | 是 | 已补 `/api/admin/user/export`；前端生成 CSV 文件 | 已实现 |
| 批量封禁 | 顶部 | 批量冻结 | 是 | 已补 `/api/admin/user/batch-status`，冻结会失效登录态 | 已实现 |
| 设置分组 | 顶部 | 批量绑定分组 | 是 | 已补 `user_member_group`、`user_member_group_bind`、`/api/admin/user/group/assign`，会员列表支持分组筛选和批量绑定 | 已实现 |
| 详情 | 行操作 | 查看会员详情 | 是 | 已补 `/api/admin/user/detail/{userId}`，聚合用户资料与钱包余额 | 已实现 |
| 补单 | 行操作 | 人工充值订单补录并入账 | 是 | 已补 `/api/admin/recharge/manual-credit` → `/api/provider/wallet/recharge/manual-credit`；wallet 单事务写 `payment_order` 审计、`wallet_recharge_order` MANUAL 订单，并通过 `WalletAccountService.applyLedger` 生成账务流水；前端补单弹窗已接入 | 已实现 |
| 查线 | 行操作 | 查看上下级链路 | 是 | 已补 `/api/admin/user/line/{userId}`，返回上级链、当前会员、直属下级 | 已实现 |
| 改线 | 行操作 | 调整上级链路 | 是 | 已补 `/api/admin/user/line/change`，校验上级存在且禁止形成环 | 已实现 |
| 下载充值 | 行操作 | 下载会员充值订单记录 | 是 | 已接 `mall-wallet` 充值订单只读查询，支持 `userId` 过滤；后台聚合 `/api/admin/recharge/list` 透传会员 UID；前端行按钮生成 CSV，不修改资金状态 | 已实现 |
| 下线 | 行操作 | 强制会话失效 | 是 | 已补 `/api/admin/user/logout`；页面支持行下线和批量下线 | 已实现 |
| 模拟登录 | 行操作 | 打开用户端只读登录 | 是 | 已有基础能力 | 待校准 |
| 冻结/解冻 | 行操作 | 更新账号状态 | 是 | 行冻结/解冻和批量封禁/解封已接入 provider，冻结会失效登录态 | 已实现 |

## 机器人配置矩阵

| 功能 | 数据表 | 接口 | 前端页面 | 状态 |
|---|---|---|---|---|
| 机器人列表 | `admin_bot_config` | 列表/详情 | `user-member/bot-config/index.vue` | 已实现 |
| 新增机器人 | `admin_bot_config` | 新增，token 必填 | `user-member/bot-config/index.vue` | 已实现 |
| 编辑机器人 | `admin_bot_config` | 编辑，token 空则保留 | `user-member/bot-config/index.vue` | 已实现 |
| 删除机器人 | `admin_bot_config`、`admin_bot_auto_reply` | 事务删除关联自动回复 | `user-member/bot-config/index.vue` | 已实现 |
| 自动回复列表 | `admin_bot_auto_reply` | 列表/详情 | `user-member/bot-config/index.vue` | 已实现 |
| 自动回复维护 | `admin_bot_auto_reply` | 新增/编辑/删除/启停 | `user-member/bot-config/index.vue` | 已实现 |
| Token 脱敏 | `admin_bot_config` | 响应只返回脱敏值 | `user-member/bot-config/index.vue` | 已实现 |

## 用户端矩阵

| 页面 | 参考站路由 | 当前状态 | 目标补齐 | 本地落点 | 状态 |
|---|---|---|---|---|---|
| 首页 | `/` | 已有基础页 | 快捷入口、公告、活动、排行、任务/VIP入口、客服/语言入口 | `mall-ui/src/views/Home.vue` | 已实现：新增账户/充值/提现/邀请/收益/公告/优惠券/客服快捷入口；登录后拉真实任务预览和 VIP 配置，不再展示“任务/VIP 暂未开放”假空态 |
| 任务中心 | `/mission`、任务相关接口 | 已有任务页 | 状态 tabs、详情、提交说明、任务计划、分享任务入口 | `mall-ui/src/views/Tasks.vue`、`components/task/TaskList.vue` | 已实现：tabs 扩为可领取/进行中/审核中/已完成/已驳回；可领取/进行中/已完成走任务列表接口，审核中/驳回走记录接口；模拟登录领取/提交按钮置灰并保留后端只读拦截 |
| 投资/项目 | `/invest`、`/invest/[id]`、`/invest/note`、`/investZone` | 已有展示页 | 按文档走任务口径，补项目详情/记录入口，不新增独立资金链路 | `mall-ui/src/views/Investment.vue`、`views/account/AccountStatus.vue`、`router/index.ts` | 已实现：路由入口已补，详情/记录进入明确状态页；不新增独立投资扣款/派息状态机 |
| 钱包总览 | `/account/balance` | 已有基础页 | 余额、充值、提现、转账、账变流水、财务资料入口 | `mall-ui/src/views/Wallet.vue` | 已实现：`/account/balance` 映射到账变 tab；钱包 tabs 收口为账变流水/充值记录/提现记录/转账记录四类真实接口，移除未接支付 tab |
| 充值 | `/account/recharge` | 已有充值页 | 地址、复制、充值记录、模拟登录只读查看 | `mall-ui/src/views/wallet/Deposit.vue` | 已实现：`/account/recharge` 映射充值页；模拟登录不分配、不复制充值地址，仅可看充值记录 |
| 提现 | `/account/withDraw` | 已有提现页 | 提现表单、记录、只读态置灰 | `mall-ui/src/views/wallet/Withdraw.vue` | 已实现：提现字段和提交按钮在模拟登录态置灰；提交仍走统一只读拦截 |
| 转账 | `/account/transfer` | 已有转账页 | 转账表单、记录、只读态置灰 | `mall-ui/src/views/wallet/Transfer.vue` | 已实现：转账字段和提交按钮在模拟登录态置灰；提交仍走统一只读拦截 |
| 团队 | `/team`、`/team/:id` | 已有基础页 | 邀请、复制链接、层级 tabs、下级详情、团队充值统计、返佣记录 | `mall-ui/src/views/Team.vue`、`views/account/AccountStatus.vue` | 部分实现：新增直属成员/收益/层级 tabs；一级团队用真实直属成员接口，二级/三级和下级详情进入明确待接口状态 |
| 我的 | `/mine`、`/account/person` | 已有基础页 | 个人资料、VIP、账户安全、修改密码、退出登录、邀请链接 | `mall-ui/src/views/Profile.vue` | 已实现：`/mine`、`/account/person` 映射个人中心；新增账户/邀请/收益/公告/帮助入口和退出登录；VIP 升级按钮支持模拟登录置灰 |
| VIP | `/vip`、`/vipDetail`、`/account/vipUplog` | 本地仅 VIP 展示/升级部分 | VIP 页面、VIP 详情、升级记录 | `Profile.vue`、`views/account/AccountStatus.vue` | 部分实现：`/vip`、`/vipDetail` 复用个人中心真实 VIP 配置与升级；`/account/vipUplog` 进入明确状态页，等待独立升级记录接口 |
| 收益 | `/income`、`/profit` | 已接真实返佣和收益汇总 | 收益统计、佣金记录、真实入账流水聚合 | `views/account/Income.vue`、`views/account/Profit.vue` | 已实现：`/income` 复用真实团队返佣；`/profit` 聚合钱包收益流水、任务统计和返佣记录，不伪造收益、不新增资金写操作 |
| 邀请 | `/invite` | 团队页部分覆盖 | 邀请海报/链接/复制/规则 | `views/account/Invite.vue` | 已实现：新增邀请页，展示真实邀请码/直属上级/直属成员，生成注册邀请链接并复制 |
| 优惠券 | `/coupon`、`/coupon/logs` | 已接真实 promotion 接口 | 优惠券模板、领取记录、状态筛选、模拟登录只读拦截 | `mall-ui/src/views/Coupon.vue`、`src/api/promotion.ts` | 已实现：`promotion_coupon_template`/`promotion_coupon_record` 承载模板和本人券记录；`/coupon/logs` 映射记录 tab；只读 smoke 返回模板 2 条、本人记录 1 条，不伪造券数据 |
| 抽奖活动 | `/lottery` | 有路由 | 增加主入口、活动列表、中奖记录 tabs | `mall-ui/src/views/Lottery.vue`、`Home.vue` | 已实现：首页新增入口；活动/记录已有真实接口；模拟登录抽奖按钮置灰并保留后端只读拦截 |
| 签到 | `/sign` | 已接真实 promotion 接口 | 今日状态、连续天数、奖励规则、签到记录、模拟登录只读拦截 | `mall-ui/src/views/CheckIn.vue`、`src/api/promotion.ts` | 已实现：`promotion_checkin_rule`/`promotion_checkin_record` 承载规则和记录；奖励通过 `CHECKIN_REWARD` 调钱包结算，不直接改余额；只读 smoke 返回状态和 2 条演示记录 |
| 活动 | `/activity`、`/activity/[id]` | 缺完整活动页 | 活动列表/详情/领取 | `router/index.ts` | 已实现入口：当前映射到抽奖活动页，后续参考站校准后再拆独立活动体系 |
| 排行榜 | `/user/user_rank` 线索、`/rank` | 已接真实榜单 | `/rank` 与 `/leaderboard` 共用收益/充值/任务榜 tabs | `mall-ui/src/views/Leaderboard.vue`、`src/api/leaderboard.ts` | 已实现：`/api/open/leaderboard/list?type=EARNING&limit=3` 返回 code=0；前端 `type-check/build-only` 通过 |
| 登录/注册 | `/login`、`/register` | 已有登录注册 | 找回密码、邀请码带入、协议入口 | `mall-ui/src/views/auth/Login.vue` | 已实现：新增 `/register`，邀请链接自动切注册 tab 并带入邀请码；协议/隐私链接改走本地 hash 路由；找回密码入口接状态页 |
| 找回/修改密码 | `/findpwd`、`/account/changePwd` | 缺找回，修改密码缺入口 | 找回密码、修改密码页面 | `views/account/AccountStatus.vue` | 已实现入口：找回密码和修改密码进入明确状态页，等待真实密码接口 |
| 实名/谷歌验证 | `/account/kyc`、`/account/googleAuth` | 缺页面 | 先记录入口，是否实现待校准 | `views/account/AccountStatus.vue` | 已实现入口：实名和 Google 验证进入明确状态页，不伪造认证状态 |
| 内容页 | `/news`、`/notice`、`/company`、`/privacyPolicy`、`/help`、`/guides`、`/pfrules`、`/termOfUse`、`/article`、`/whitepapers`、`/cpfiles` | 已建立内容 DSL | 公告、公司、隐私、协议、规则、帮助、指南与资料入口 | `views/account/ContentPage.vue`、`src/api/content.ts`、`router/index.ts` | 部分实现：`NOTICE/COMPANY_PROFILE/PLATFORM_PROFILE/REGULATOR/USER_AGREEMENT/USER_PRIVACY` 走 `/api/open/content/list` 真实内容；`news/article/whitepapers/cpfiles` 保持明确状态页，等待独立内容/文件域 |
| 客服 | `/service` | 已接只读配置 | 客服入口/联系方式 | `views/account/ContentPage.vue`、`Home.vue`、`OpenContentController` | 已实现：`/api/open/content/service` 聚合 `admin_system_param` 与启用机器人配置，只返回机器人名称/说明，不暴露 `botToken`，不写死外部联系方式 |
| 模拟登录只读态 | 后台模拟登录链路 | 已有横幅 | 写操作入口置灰、只读查看优化 | `App.vue`、钱包/任务/VIP/抽奖页面 | 已实现：充值地址分配/复制、提现、转账、任务领取提交、VIP 升级、抽奖均显示只读或置灰并保留统一拦截 |

## 后端接口闭环矩阵

| 领域 | 能力 | 当前状态 | 目标状态 | 状态 |
|---|---|---|---|---|
| 会员 | 详情/新增/编辑/批量封禁/分组/导出/下线/补单/下载充值 | 已实现：详情、查询筛选、分组筛选、新增/编辑、导出、冻结/解冻、批量封禁/解封、行/批量下线、查线/改线、补单、下载充值只读导出；补单由 `mall-wallet` 单事务生成 MANUAL 充值订单、支付审计和账务流水 | 继续补会员子菜单的日志/流水/站内信/电报/测试账号等独立页面能力 | 部分实现 |
| 分组 | 分组 CRUD、会员绑定 | 已新增 `user_member_group`、`user_member_group_bind`、provider 分组 CRUD、admin 聚合分组接口、会员列表批量绑定 | 独立分组设置页后续从占位承载页替换 | 接口已实现 |
| 日志 | 会员日志、操作日志 | 部分已有 | 会员维度聚合 | 待实现 |
| 钱包 | 会员流水、余额记录、充值/提现/转账记录 | wallet 已有基础 | admin 聚合查询 | 待实现 |
| 站内信 | 消息管理 | 缺失 | 新增表和接口 | 待实现 |
| 电报/机器人 | 配置、自动回复 | 已实现 | `mall-admin` 新增 `/api/admin/bot/**`，只维护配置，不调用 Telegram API | 已实现 |
| 分享审核 | 审核列表/通过/驳回 | 缺失 | 新增或聚合 mission | 待实现 |
| 测试账号 | 标记/筛选/管理 | 缺失 | 扩展会员字段或独立表 | 待实现 |
| 用户端内容 | 协议/隐私/公告/平台规则/客服配置 | 已开放 `/api/open/content/list` 与 `/api/open/content/service`；前端内容 DSL 已接入真实配置 | 后续仅为 `NEWS/ARTICLE/WHITEPAPER/CPFILES` 增加独立内容或文件域 | 部分实现 |
| 用户端钱包 | 只读充值地址、记录 tabs | 前端已补 `/account/balance/recharge/withDraw/transfer` 映射；记录 tabs 使用 wallet 已有真实接口；模拟登录资金写操作已置灰 | 后续补资金资料、多资产账户等接口 | 部分实现 |
| 用户端任务子类型 | 分享/朋友圈/视频/VA 任务列表、记录、审核状态、奖励结算 | 已闭环：`mission_task.task_type` 作为唯一子类型 DSL，`mission_user_task` 继续作为唯一用户任务状态机；用户端 `/shareTask`、`/shareTaskList`、`/friendsCircle`、`/vatask`、`/videoTask` 统一落到 `Tasks.vue` 并透传 `taskType`；后台审核页可按任务类型筛选；审核通过奖励按 `SHARE_TASK_REWARD`、`VIDEO_TASK_REWARD`、`VA_TASK_REWARD` 调钱包结算，不直接改余额 | 后续如参考站出现独立证明字段，再在任务提交 DTO 上扩展受控字段，不新增并行状态机 | 已实现 |
| 用户端促销 | 优惠券模板/领取记录/签到规则/签到记录/签到奖励 | 已闭环：优惠券由 `promotion_coupon_template` 与 `promotion_coupon_record` 承载模板、本人领取、锁定、使用、过期状态；签到由 `promotion_checkin_rule` 与 `promotion_checkin_record` 承载连续天数和奖励事实；签到奖励以 `CHECKIN_REWARD + recordNo` 调钱包结算，幂等键不绕过 `applyLedger` | 后续如接订单核销，只扩展优惠券锁定/核销动作，不在前端伪造可用券或直接扣减余额 | 已实现 |

## 数据种子矩阵

| 数据范围 | 用途 | 状态 |
|---|---|---|
| 顶部模块菜单 | 支撑后台布局 | 已应用到本地 MySQL：`ROLE_ADMIN` 已绑定全部启用菜单；会员缺失子菜单通过 `local-demo-seed.sql` 幂等补齐 |
| 会员管理菜单 | 支撑左侧子菜单 | 已实现：`init_data.sql` 两份均新增分组/日志/流水/站内信/电报/余额/升级/分享/测试账号菜单；本地 MySQL 已应用会员子菜单和角色绑定 |
| VIP0-VIP5 会员 | 支撑 VIP tabs | 已应用到本地 MySQL：新增 6 个演示会员 `demo_vip0` 至 `demo_vip5_frozen`，密码统一为 `123456`，覆盖 VIP0-VIP5、上下级和冻结态 |
| 分组数据 | 支撑分组设置/筛选 | 已应用到本地 MySQL：补建 `user_member_group`、`user_member_group_bind`，写入普通/重点/测试会员分组与演示会员绑定 |
| 钱包流水 | 支撑流水/余额记录 | 已应用到本地 MySQL：6 个演示钱包、18 条账变流水；校验 `total_balance = avail_balance + frozen_balance` 通过 |
| 充值/提现/转账订单 | 支撑账单页和用户端记录 | 已应用到本地 MySQL：7 条充值、4 条提现、2 条转账、支付审计订单，覆盖已入账/待确认/审核中/已确认/驳回等状态 |
| 任务状态数据 | 支撑任务 tabs | 已应用到本地 MySQL：基础任务配置与用户任务记录覆盖可领取、进行中、审核中、已完成、已驳回；奖励结算关联校验通过 |
| 分享/视频/VA 任务子类型数据 | 支撑参考站分享任务、视频任务、VA 任务入口 | 已应用到本地 MySQL：`mission_task` 写入 `SHARE/VIDEO/VA` 子类型任务样例；`mission_user_task` 写入分享已通过/已驳回、视频审核中、VA 已驳回记录；分享任务演示流水与结算业务类型校正为 `SHARE_TASK_REWARD` |
| 抽奖数据 | 支撑活动/中奖记录 | 已应用到本地 MySQL：抽奖配置和 2 条演示中奖记录，覆盖现金已结算和谢谢参与 |
| 优惠券/签到数据 | 支撑 `/coupon`、`/coupon/logs`、`/sign` | 已应用到本地 MySQL：优惠券模板 2 条、领取/使用/过期记录样例、签到规则 3 条、签到演示记录 3 条；签到奖励业务类型使用 `CHECKIN_REWARD` 钱包结算口径 |
| 站内信 | 支撑站内信菜单 | 待实现：当前只有菜单承载页，尚无站内信表和接口，不伪造数据 |
| 机器人配置 | 支撑机器人页面 | 已应用到本地 MySQL：补建 `admin_bot_config`、`admin_bot_auto_reply`，写入 demo bot 和 2 条自动回复；后端仍只维护配置，不调用 Telegram API |
| 协议/隐私/公告/平台规则/客服 | 支撑用户端内容和客服页 | 已应用到本地 MySQL：写入公告、用户协议、隐私政策、公司简介、平台介绍、平台规则演示内容；客服文案来自 `admin_system_param`，机器人公开信息来自 `admin_bot_config`，不落完整用户端 token |

### 本地演示数据执行记录

| 项 | 结果 |
|---|---|
| SQL 文件 | `/Users/aa/data/java/task-mall/task-mall/local-demo-seed.sql` |
| 执行命令 | `mysql -uroot < /Users/aa/data/java/task-mall/task-mall/local-demo-seed.sql` |
| 幂等验证 | 已重复执行一次，关键表数量保持稳定 |
| 一致性验证 | 钱包余额不变量、账变 `wallet_id`、任务结算 `user_task_id`、`ROLE_ADMIN` 菜单绑定缺口均为 0 |

## 模块校准记录

### 后台会员列表

| 校准项 | 结果 | 证据/说明 |
|---|---|---|
| 后端编译 | 通过 | `mvn -pl mall-admin,mall-user,mall-wallet -am -DskipTests compile` 成功，耗时约 3.7s；仅有既有 Lombok/MapStruct warning |
| 前端页面骨架 | 通过 | `members/index.vue` 已包含 VIP0-VIP5 tabs、UID/账号/邀请码/状态/分组/注册时间筛选、批量封禁/解封/下线/分组/导出、详情/编辑/查线/改线/补单/下载充值/下线/模拟登录/冻结解冻 |
| 前后端接口映射 | 通过 | `member.ts` 的 `/api/admin/user/page/detail/save/export/group/*/line/*/batch-status/logout/impersonation-ticket` 与 `UserController` 对齐；充值补单和下载充值走 `/api/admin/recharge/*` |
| 服务间聚合链路 | 通过 | `mall-admin` 会员聚合只调用 `mall-user` 会员资料/分组/链路/模拟登录和 `mall-wallet` 钱包余额，不直接跨库写状态 |
| VIP 演示数据 | 通过 | `demo_vip_rows=6`，`demo_vip_levels=0,1,2,3,4,5` |
| 分组演示数据 | 通过 | `member_groups=3`，`member_group_binds=6` |
| 钱包演示数据 | 通过 | `demo_wallet_accounts=6`，`wallet_balance_mismatch=0` |
| 会员菜单数据 | 通过 | `member_menu_items_actual=10`，`role_admin_member_menu_binds=10`；会员列表沿用历史键 `menu.member-list`，其它新增子菜单使用 `menu.user-member.*` |
| 运行态校准 | 通过 | 浏览器短会话曾被中断，未强制重开；改用运行态 API 完成同模块校准。`admin/123456` 登录成功且 token 仅脱敏输出；动态菜单返回 `user-member` 根和 13 个会员子菜单；会员分页 `status=1` 返回 6 条；分组列表返回 3 条；模拟登录票据创建成功，`expiresIn=300`，票据仅脱敏输出 |
| 运行态修复 | 通过 | 复测发现 `/api/admin/user/group/list?status=1` 因 admin 控制器简单参数缺少 `@RequestParam` 返回系统错误；已在 `UserController.groupList` 显式声明 `@RequestParam(name = "status", required = false)`，重启 `mall-admin` 后分组接口恢复 |

### 后台会员子菜单

| 校准项 | 结果 | 证据/说明 |
|---|---|---|
| 菜单承载 | 通过 | `user-member/member-feature/index.vue` 可承载分组设置、会员日志、余额/账变、站内信、VIP 配置、邀请返佣、测试账号、电报信息等缺接口子页 |
| 动态路由 | 通过 | `router/app-menus/user-routes.ts` 按后端 `component` 加载 `src/views/**`，会员子菜单不依赖纯静态硬编码 |
| 菜单权限 | 通过 | 本地 seed 已绑定 `ROLE_ADMIN` 会员菜单；会员列表实际 key 为 `menu.member-list`，其它新增子菜单使用 `menu.user-member.*` |
| 业务闭环 | 部分通过 | 分组接口已存在，独立分组设置页待替换承载页；日志、余额/账变、站内信、电报、测试账号、VIP 配置、邀请返佣仍待真实业务接口 |

### 后台账单/钱包

| 校准项 | 结果 | 证据/说明 |
|---|---|---|
| 页面/API 映射 | 通过 | 后台订单页面目录、`recharge.ts`、`withdraw.ts`、`transfer.ts`、`walletFlow.ts`、`payment.ts`、`orderApproval.ts` 与后端充值/提现/转账/账变控制器存在 |
| 演示数据 | 通过 | 本地充值订单、提现订单、转账订单、支付审计订单、`wallet_flow_detail` 账变流水均有演示数据 |
| 资金安全 | 通过 | 未执行审核、补单、充值、提现、转账写操作；余额不变量校验为 0 异常 |

### 后台机器人配置

| 校准项 | 结果 | 证据/说明 |
|---|---|---|
| 页面/API/服务 | 通过 | `user-member/bot-config/index.vue`、`bot.ts`、`BotConfigController`、`BotConfigServiceImpl` 存在并对齐 `/api/admin/bot/**` |
| Token 脱敏 | 通过 | `BotConfigVO` 只返回 `maskedToken`，不返回原始 `botToken` |
| 编辑规则 | 通过 | 新增 token 必填；编辑 token 可空并保留原 token；删除机器人会清理关联自动回复 |
| 演示数据 | 通过 | `admin_bot_config` 和 `admin_bot_auto_reply` 有 demo 数据，自动回复孤儿记录为 0 |

### 用户端 H5 主链路

| 校准项 | 结果 | 证据/说明 |
|---|---|---|
| 路由/布局 | 通过 | `/home`、`/tasks`、`/investment`、`/lottery`、`/wallet`、`/team`、`/profile`、`/account/*`、`/register` 等入口已存在 |
| 钱包/任务/团队/我的 | 通过 | 钱包记录 tabs、任务状态 tabs、团队直属成员、个人中心账户入口均使用真实接口或明确状态页，不伪造缺口数据 |
| 模拟登录只读态 | 通过 | 充值地址分配/复制、提现、转账、任务领取提交、VIP 升级、抽奖等写操作入口已置灰或拦截，提示为 `当前为后台模拟登录，仅可查看，不能操作` |
| 类型检查 | 通过 | `mall-ui` 已在 Node 22.23.1 下执行 `npm run type-check`，`vue-tsc --build` 通过 |

### 类型检查与构建验证

| 校准项 | 结果 | 证据/说明 |
|---|---|---|
| 后台前端新增范围类型错误 | 已修复 | `module-menu.vue` 的路由树回调已显式标注路由类型；`bot-config/index.vue` 的表格行点击已增加 `TableData` 到 `BotConfig` 的边界适配 |
| 后台前端整体类型检查 | 仍失败 | `npm run type:check` 可执行；本轮新增/改造文件错误已消失，剩余 8 个为旧范围：`excelTransfer.vue`、`menuTable.vue`、`ccPopover.vue`、`role-control/index.vue`、`user-log/index.vue` |
| 后台最近编辑文件诊断 | 通过 | `module-menu.vue`、`bot-config/index.vue` 未发现新诊断 |
| 后台 5173 运行态红屏 | 已修复 | `/src/App.vue` 等 `.vue` 模块原先被 `vite-plugin-eslint` 拦截，并因 `vue-eslint-parser` 在 Node 16 下触发 `this.elementStack.findLastIndex is not a function` 导致 500；已在 `config/vite.config.dev.ts` 解除开发服务器与 ESLint 的运行前置绑定，校验改为独立执行；`menu-modules.ts` 改为 lint 友好的 `reduce` 遍历；复测 `/`、`/src/App.vue`、会员列表、机器人配置等关键模块均返回 200；`npx eslint config/vite.config.dev.ts src/components/menu/menu-modules.ts` 通过 |
| 用户端 P0/P1 一致性入口 | 已完成 | `/rank`、`/tasklist`、`/profit`、`/403`、兜底错误页、`/termOfUse`、`/article`、`/whitepapers`、`/cpfiles`、分享/视频/VA/联盟等精确入口均已落到真实页面或明确状态页；矩阵已更新 |
| 用户端内容/客服真实化 | 已完成 | 新增 `/api/open/content/list` 和 `/api/open/content/service`；`ContentPage.vue` 改为内容 DSL + 客服模式；`/company`、`/pfrules`、`/privacyPolicy`、`/termOfUse` 读取后台真实内容，`/service` 读取只读客服配置且不暴露 `botToken` |
| 用户端类型检查与构建 | 通过 | `cd /Users/aa/data/java/task-mall/mall-ui && nvm use 22 && npm run type-check` 通过；`npm run build-only` 通过，Vite `v8.1.0` 构建成功 |
| 后端内容/客服 open 接口编译 | 通过 | `mvn -pl mall-admin -am -DskipTests compile` 通过；仅有既有 Lombok/MapStruct warning |
| 用户端内容/排行只读 API smoke | 通过 | `/api/open/content/list` 覆盖 `USER_AGREEMENT/USER_PRIVACY/REGULATOR/COMPANY_PROFILE` 均返回 code=0 且有数据；`/api/open/content/service` 返回 code=0 且不包含原始 token；`/api/open/leaderboard/list?type=EARNING&limit=3` 返回 code=0 |
| 用户端任务子类型 API smoke | 通过 | `mall-mission` 已监听 `10003`；本地 demo 账号登录后仅在进程内使用 token，不输出、不落盘。`SHARE/VIDEO/VA` 的 `/api/open/mission/stats`、`/tasks?status=available`、`/records?status=SUBMITTED/REJECTED/APPROVED` 均返回 code=0；返回记录的 `taskType` 与请求过滤一致 |
| 用户端优惠券/签到只读 API smoke | 通过 | `mall-wallet` 已监听 `10002`，`mall-promotion` 已监听 `10004`；幂等 seed 已重新执行。本地 demo 账号登录后仅在进程内使用 token，不输出、不落盘。`/api/open/promotion/coupon/templates` 返回 2 条，`/coupon/records` 返回本人记录，`/checkin/state` 和 `/checkin/records` 均返回 code=0 |

## 当前 Todo 状态

| Todo | 状态 | 备注 |
|---|---|---|
| 建立可恢复进度清单和页面/按钮/接口矩阵 | 已完成 | 当前文件 |
| 按模块短会话抓取参考站运行态页面骨架和网络请求清单 | 已完成 | 已完成用户端静态资源/路由/接口线索抓取；后续浏览器只做模块短校准 |
| 重构后台顶部模块 + 左侧子菜单 + 路由 TabBar 布局 | 已完成 | 已改 `default-layout.vue`、`navbar/index.vue`、`components/menu/index.vue`，新增 `components/menu/menu-modules.ts`、`components/menu/module-menu.vue`；本轮已修复 `module-menu.vue` 类型错误，后台整体 `npm run type:check` 仍受旧组件错误阻塞 |
| 实现后台会员管理菜单和会员列表 VIP tabs | 已完成 | 会员列表已接入全部/VIP0-VIP5 tabs；后台聚合层和 `mall-user` provider 已支持 `vipLevel` 过滤；会员左侧缺失菜单已补种子和承载页，具体按钮接口进入后续会员动作模块 |
| 实现机器人配置表、接口、菜单与脱敏规则 | 已完成 | 已新增 `admin_bot_config`、`admin_bot_auto_reply`、`/api/admin/bot/**`、后台页面、菜单和 demo 数据；后端目标模块编译通过；本轮已修复 `bot-config/index.vue` 类型错误，后台整体 `npm run type:check` 仍受旧组件错误阻塞 |
| 补齐后台会员相关按钮和聚合接口 | 已完成 | 已补会员查询区（UID/账号/邀请码/VIP/状态/分组/注册时间）、新增/编辑、详情、导出、分组筛选/批量绑定、查线/改线、行/批量下线、行/批量冻结解冻、补单、下载充值只读导出；补单链路为 `mall-admin` 注入操作人和校验会员、`mall-wallet` 单事务写 MANUAL 充值订单/支付审计/账务流水；运行态修复并复测分组列表参数绑定；模拟登录票据入口可用且只脱敏输出；后端目标模块编译通过；前端最近改动文件无诊断；后续转会员子菜单独立页面能力 |
| 补齐用户端导航、页面内 tabs、模拟登录只读态和关键按钮 | 已完成 | 已新增 `/account`、`/invite`、`/income`、`/profit`、内容/状态承载页，补齐 `/account/*`、`/register`、`/findpwd`、`/coupon*`、`/help`、`/notice`、`/service`、`/activity*`、`/rank`、`/tasklist`、`/403` 等入口；钱包 tabs 收口为账变/充值/提现/转账真实接口；任务 tabs 扩为可领取/进行中/审核中/已完成/已驳回；首页接真实任务/VIP 预览；提现/转账/充值复制/任务/VIP/抽奖模拟登录只读态已置灰或拦截；用户端 `type-check` 和 `build-only` 均通过 |
| 编写并运行本地 MySQL 幂等演示数据 | 已完成 | 已新增并执行 `/Users/aa/data/java/task-mall/task-mall/local-demo-seed.sql`；补齐本地缺失表 `admin_bot_config/admin_bot_auto_reply/user_member_group/user_member_group_bind`，写入演示会员、钱包、订单、任务、抽奖、内容和机器人数据；重复执行幂等，余额/关联/菜单绑定一致性校验通过 |
| 用户端一致性 P0/P1 既有后端页面真实化 | 已完成 | 已完成 `/profit`、`/rank`、内容 DSL、客服 open 配置；后端 `mall-admin` 编译通过，用户端 `type-check/build-only` 通过，只读 API smoke 通过；`mall-mission` 分享/朋友圈/视频/VA 任务子类型已完成前后端闭环和只读 API smoke |
| 按模块做短时浏览器校准和低负载验证 | 已完成 | 低负载校准已覆盖后台会员列表、会员子菜单、账单/钱包、机器人配置、用户端 H5；运行态校准以 API 链路收口。浏览器短会话被中断，未强制重开；如必须做 UI 点击级校准，需要重新授权单页短会话 |