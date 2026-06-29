package com.mall.user.controller.open;

import com.mall.common.core.result.Result;
import com.mall.user.model.vo.TeamMembersVo;
import com.mybatisflex.core.paginate.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/open/user/team/")
public class UserTeamOpenController {

    @GetMapping("/members")
    public Result<Page<TeamMembersVo>> teamMembers(){
        // todo
        return Result.ok();
    }
}
