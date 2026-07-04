# 用户端一致性矩阵

> 目标：用固定矩阵推进参考站用户端与本地用户端对齐，避免反复浏览器抓取和无产出思考。
>
> 参考来源：`https://www.rwkaifa1.com/assets/index-d652c788.js` 静态主包提取；不保存完整用户 token，不执行参考站写操作。
>
> 状态枚举：`已闭环`、`入口已映射`、`前端缺`、`后端缺`、`待确认`。

## 执行规则

- 先补入口和能力映射，再补后端状态机。
- 能复用现有后端的页面必须真实化，不能长期停留在空壳状态页。
- 涉及资金、奖励、提现、转账、充值、任务结算的写操作必须走业务记录和钱包结算，不直接改余额。
- 后台模拟登录态的用户端写操作必须拦截，提示：`当前为后台模拟登录，仅可查看，不能操作`。
- 浏览器只用于本地短 smoke，不用于参考站漫游抓取。

## 页面与能力矩阵

| 参考路由 | 参考能力 | 本地路由 | 本地页面落点 | 本地 API | 后端服务/表 | 状态 | 最小验证方式 |
|---|---|---|---|---|---|---|---|
| `/account` | 账户中心 | `/account` | `mall-ui/src/views/account/AccountHub.vue` | `mall-ui/src/api/user.ts`, `wallet.ts` | `mall-user` open user, `mall-wallet` wallet | 已闭环 | 登录后访问 `#/account`，检查账户、钱包、邀请入口 |
| `/mine` | 我的/个人中心 | `/profile`，`/mine` redirect | `mall-ui/src/views/Profile.vue` | `user.ts` | `UserInfoOpenController`, VIP 配置 | 已闭环 | 登录后访问 `#/mine`，应进入个人中心 |
| `/login` | 登录 | `/login` | `mall-ui/src/views/auth/Login.vue` | `auth.ts` | `UserAccountOpenController` | 已闭环 | 登录 demo 账号，返回 token 并进入首页 |
| `/register` | 注册 | `/register` | `mall-ui/src/views/auth/Login.vue` | `auth.ts` | `UserAccountOpenController` | 已闭环 | 打开注册 tab，不提交参考站写操作 |
| `/findpwd` | 找回密码 | `/findpwd` | `AccountStatus.vue` | 无 | 后端缺找回密码验证码/令牌流程 | 后端缺 | 页面展示待接入，不允许前端本地重置密码 |
| `/income` | 邀请/收益 | `/income` | `mall-ui/src/views/account/Income.vue` | `user.ts#getTeamEarnings` | `UserTeamOpenController`, invite commission | 已闭环 | 登录后访问 `#/income`，拉取返佣记录 |
| `/profit` | 收益汇总/利润 | `/profit` | `mall-ui/src/views/account/Profit.vue` | `wallet.ts#getWalletFlows`, `user.ts#getTeamEarnings`, `mission.ts#getMissionStats` | 钱包流水、任务统计、返佣 | 已闭环 | 访问 `#/profit`，展示真实流水聚合；`type-check/build-only` 通过 |
| `/mission` | 任务中心 | `/tasks`，`/mission` redirect | `mall-ui/src/views/Tasks.vue` | `mission.ts` | `MissionOpenController`, `mission_task`, `mission_user_task` | 已闭环 | 登录后访问 `#/mission`，任务 tabs 可拉取 |
| `/tasklist` | 任务列表 | `/tasklist` | `Tasks.vue` | `mission.ts` | `mall-mission` | 已闭环 | 访问 `#/tasklist`，进入任务列表并拉取真实任务 tabs |
| `/shareTask` | 分享任务 | `/shareTask` | `mall-ui/src/views/Tasks.vue` | `mission.ts#getMissionStats/getMissionTasks/getMissionRecords(taskType=SHARE)` | `mall-mission`：`mission_task.task_type=SHARE`，`mission_user_task` 状态机，钱包 `SHARE_TASK_REWARD` 结算 | 已闭环 | 本地只读 smoke：`/api/open/mission/stats/tasks/records?taskType=SHARE` 均返回 code=0，列表/记录仅返回 `SHARE` |
| `/shareTaskList` | 分享任务记录/列表 | `/shareTaskList` | `Tasks.vue` | `mission.ts#getMissionRecords(taskType=SHARE)` | `mall-mission`：复用唯一用户任务记录表 | 已闭环 | 默认进入完成记录 tab；只读 smoke 验证 `APPROVED/REJECTED` 记录过滤为 `SHARE` |
| `/friendsCircle` | 朋友圈/分享任务 | `/friendsCircle` | `Tasks.vue` | `mission.ts#getMissionTasks/getMissionRecords(taskType=SHARE)` | 分享任务统一归入 `SHARE` 子类型，不新增并行状态机 | 已闭环 | 访问 `#/friendsCircle` 进入分享任务页；API smoke 与 `/shareTask` 同口径 |
| `/vatask` | VA 任务 | `/vatask` | `Tasks.vue` | `mission.ts#getMissionStats/getMissionTasks/getMissionRecords(taskType=VA)` | `mall-mission`：`mission_task.task_type=VA`，审核通过后钱包 `VA_TASK_REWARD` 结算 | 已闭环 | 本地只读 smoke：`/api/open/mission/stats/tasks/records?taskType=VA` 均返回 code=0，列表/记录仅返回 `VA` |
| `/videoTask` | 视频任务 | `/videoTask` | `Tasks.vue` | `mission.ts#getMissionStats/getMissionTasks/getMissionRecords(taskType=VIDEO)` | `mall-mission`：`mission_task.task_type=VIDEO`，审核通过后钱包 `VIDEO_TASK_REWARD` 结算 | 已闭环 | 本地只读 smoke：`/api/open/mission/stats/tasks/records?taskType=VIDEO` 均返回 code=0，列表/记录仅返回 `VIDEO` |
| `/invest` | 投资入口 | `/investment`，`/invest` redirect | `mall-ui/src/views/Investment.vue` | `mission.ts#getInvestProjects` | `mission_goods` 的 `INVEST_PROJECT` 展示口径 | 已闭环 | 访问 `#/invest`，展示真实投资项目列表 |
| `/investZone` | 投资专区 | `/investment`，`/investZone` redirect | `Investment.vue` | `mission.ts#getInvestProjects` | `mall-mission` | 已闭环 | 访问 `#/investZone`，展示项目列表 |
| `/investProductDetail` | 投资产品详情 | `/investProductDetail`，`/invest/:id` | `AccountStatus.vue` | 无 | `mall-mission` 只有列表，无详情/购买/持仓状态机 | 后端缺 | 先补只读详情；若参考涉及购买/派息，再建投资状态机 |
| `/lottery` | 抽奖 | `/lottery` | `mall-ui/src/views/Lottery.vue` | `promotion.ts` | `PromotionLotteryOpenController`, promotion lottery tables | 已闭环 | 登录后访问 `#/lottery`，可拉活动/记录；模拟登录写操作拦截 |
| `/activity` | 活动 | `/activity` redirect `/lottery` | `Lottery.vue` | `promotion.ts` | `mall-promotion` lottery | 入口已映射 | 访问 `#/activity` 进入抽奖活动 |
| `/marketing` | 营销推广 | `/marketing` | `AccountStatus.vue` | 无 | 缺 promotion/content 对应 open 配置 | 后端缺 | 当前显示缺口，后续归入 promotion/content |
| `/coupon` | 优惠券 | `/coupon` | `mall-ui/src/views/Coupon.vue` | `promotion.ts#getCouponTemplates/getCouponRecords/claimCoupon` | `mall-promotion`：`promotion_coupon_template`、`promotion_coupon_record`；领取/锁定/使用/过期状态留在 promotion 域 | 已闭环 | 本地只读 smoke：`/api/open/promotion/coupon/templates`、`/coupon/records` 均返回 code=0；模拟登录领取由统一只读拦截 |
| `/sign` | 签到 | `/sign` | `mall-ui/src/views/CheckIn.vue` | `promotion.ts#getCheckinState/getCheckinRecords/checkin` | `mall-promotion`：`promotion_checkin_rule`、`promotion_checkin_record`；签到奖励以 `CHECKIN_REWARD` 调钱包结算 | 已闭环 | 本地只读 smoke：`/api/open/promotion/checkin/state`、`/checkin/records` 均返回 code=0；奖励链路走 wallet settlement，不直接改余额 |
| `/team` | 我的团队 | `/team` | `mall-ui/src/views/Team.vue` | `user.ts#getTeamMembers`, `getTeamEarnings` | `UserTeamOpenController` | 已闭环 | 登录后访问 `#/team`，拉直属成员和收益 |
| `/team/:id` | 下级详情 | `/team/:id` | `AccountStatus.vue` | 无 | 后端只有直属列表，缺下级详情与层级统计 | 后端缺 | 当前展示缺口，不越权查询 |
| `/union` | 联盟/代理 | `/union` | `AccountStatus.vue` | 无 | 缺代理职位、薪资、领取状态 | 后端缺 | 新增 user/agent 域后验证职位与收益 |
| `/rank` | 排行榜 | `/rank`，兼容 `/leaderboard` | `mall-ui/src/views/Leaderboard.vue` | `leaderboard.ts` | `OpenLeaderboardController` | 已闭环 | `/api/open/leaderboard/list?type=EARNING&limit=3` 返回 code=0 |
| `/notice` | 公告 | `/notice` | `ContentPage.vue` | `content.ts#getNotices` | `OpenContentController#notices`, `admin_content_item` | 已闭环 | 访问 `#/notice`，拉公告列表 |
| `/news` | 新闻 | `/news` | `ContentPage.vue` 状态页 | 无 | 内容枚举缺 `NEWS`，open 只暴露公告 | 后端缺 | 扩展内容类型和 open 查询后验证 |
| `/article` | 文章详情/列表 | `/article` | `ContentPage.vue` 状态页 | 无 | 内容类型缺 `ARTICLE` | 后端缺 | 扩展内容类型和 open 查询后验证 |
| `/help` | 帮助中心 | `/help` | `ContentPage.vue` | `content.ts#getContentItems(PLATFORM_PROFILE)` | `OpenContentController`, `admin_content_item.PLATFORM_PROFILE` | 入口已映射 | 当前复用平台介绍内容；独立 HELP 类型后续扩展 |
| `/guides` | 新手指南 | `/guides` | `ContentPage.vue` | `content.ts#getContentItems(PLATFORM_PROFILE)` | `OpenContentController`, `admin_content_item.PLATFORM_PROFILE` | 入口已映射 | 当前复用平台介绍内容；独立 GUIDE 类型后续扩展 |
| `/company` | 公司介绍 | `/company` | `ContentPage.vue` | `content.ts#getContentItems(COMPANY_PROFILE)` | `OpenContentController`, `admin_content_item.COMPANY_PROFILE` | 已闭环 | `/api/open/content/list?type=COMPANY_PROFILE` 返回 code=0 且有内容 |
| `/service` | 客服 | `/service` | `ContentPage.vue` 客服模式 | `content.ts#getServiceConfig` | `OpenContentController#serviceConfig`, `admin_system_param`, `admin_bot_config` | 已闭环 | `/api/open/content/service` 返回 code=0；不暴露 `botToken` |
| `/pfrules` | 平台规则 | `/pfrules` | `ContentPage.vue` | `content.ts#getContentItems(REGULATOR)` | `OpenContentController`, `admin_content_item.REGULATOR` | 已闭环 | `/api/open/content/list?type=REGULATOR` 返回 code=0 且有内容 |
| `/privacyPolicy` | 隐私政策 | `/privacyPolicy` | `ContentPage.vue` | `content.ts#getContentItems(USER_PRIVACY)` | `OpenContentController`, `admin_content_item.USER_PRIVACY` | 已闭环 | `/api/open/content/list?type=USER_PRIVACY` 返回 code=0 且有内容 |
| `/termOfUse` | 用户协议/使用条款 | `/termOfUse`，兼容 `/terms` | `ContentPage.vue` | `content.ts#getContentItems(USER_AGREEMENT)` | `OpenContentController`, `admin_content_item.USER_AGREEMENT` | 已闭环 | `/api/open/content/list?type=USER_AGREEMENT` 返回 code=0 且有内容 |
| `/whitepapers` | 白皮书 | `/whitepapers` | `ContentPage.vue` 状态页 | 无 | 缺内容类型或文件资料表 | 后端缺 | 补内容/文件域后验证 |
| `/cpfiles` | 文件资料 | `/cpfiles` | `ContentPage.vue` 状态页 | 无 | 缺文件资料表、下载/权限 open API | 后端缺 | 补文件域后验证 |
| `/403` | 无权限页 | `/403` | `mall-ui/src/views/ErrorPage.vue` | 无 | 前端静态能力 | 已闭环 | 访问 `#/403`，展示无权限说明 |
| `/:error(.*)` | 兜底错误页 | `/:error(.*)` | `mall-ui/src/views/ErrorPage.vue` | 无 | 前端静态能力 | 已闭环 | 访问不存在 hash，展示兜底页 |

## P0/P1 已完成校准

1. `/rank` 已映射到 `Leaderboard.vue`，保留 `/leaderboard` 兼容入口。
2. `/tasklist` 已映射到 `Tasks.vue`，保留 `/tasks` 兼容入口。
3. `/profit` 已实现真实收益汇总页：读取钱包流水、任务统计、返佣记录，只读聚合。
4. `/403` 与兜底错误页已完成。
5. `/termOfUse`、`/company`、`/pfrules`、`/privacyPolicy` 已接入 `admin_content_item` 真实内容；`/help`、`/guides` 先复用 `PLATFORM_PROFILE`，保留独立内容类型缺口。
6. `/service` 已接入后台系统参数与机器人配置只读接口，不暴露 `botToken`。
7. `shareTask/shareTaskList/friendsCircle/vatask/videoTask` 已进入 `mall-mission` 子类型闭环：前端统一落到 `Tasks.vue`，通过 `taskType=SHARE/VIDEO/VA` 过滤统计、列表与记录；奖励结算业务类型已区分为 `SHARE_TASK_REWARD`、`VIDEO_TASK_REWARD`、`VA_TASK_REWARD`，不绕过钱包结算链路。
8. `/coupon`、`/coupon/logs` 已进入 `mall-promotion` 优惠券闭环：前端落到 `Coupon.vue`，读取模板与本人领取记录；领取写操作由登录态取用户，模拟登录态拦截。
9. `/sign` 已进入 `mall-promotion` 签到闭环：前端落到 `CheckIn.vue`，读取签到状态、规则和记录；签到奖励以 `CHECKIN_REWARD` 通过钱包结算入账，不直接改余额。