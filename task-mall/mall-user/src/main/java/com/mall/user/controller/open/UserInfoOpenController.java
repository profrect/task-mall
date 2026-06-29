package com.mall.user.controller.open;

import com.mall.common.core.result.Result;
import com.mall.user.model.dto.UserInfoDTO;
import com.mall.user.model.dto.VipLevelUpDTO;
import com.mall.user.model.vo.UserDetailVO;
import com.mall.user.model.vo.VipLevelConfigVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/open/user/")
public class UserInfoOpenController {

    /**
     * 用户详细信息（根据sa-token解析出用户信息，进行查询）
     * @return Result
     */
    @GetMapping("/detail")
    public Result<UserDetailVO> currentUser(){
        // todo
        return Result.ok();
    }

    /**
     * 修改用户信息
     * @return Result
     */
    @PutMapping("/update")
    public Result<Void> updateUserInfo(UserInfoDTO userInfoDTO){
        return Result.ok();
    }

    /**
     * 查询VIP 等级及其奖励等参数（需要远程调用管理中心）
     * @return Result
     */
    @GetMapping("/vip-level")
    public Result<List<VipLevelConfigVO>> vipLevelList(){
        // todo
        return Result.ok();
    }

    /**
     * 用户提升vip等级
     * @return Result
     */
    @PostMapping("/vip-level-up")
    public Result<Void> vipLevelUp(@RequestBody @Validated VipLevelUpDTO levelUpDTO){
        // todo 先检查用户信息，然后再看用户需要提升到哪个等级，从该等级中获取需要的金额。先去钱包中心扣款，然后再修改数据库中的VIP等级数据
        return Result.ok();
    }
}
