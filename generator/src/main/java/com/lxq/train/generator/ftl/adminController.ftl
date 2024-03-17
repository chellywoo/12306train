package com.lxq.train.${module}.controller.admin;

import com.lxq.train.common.resp.CommonResp;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.${module}.req.${Domain}QueryReq;
import com.lxq.train.${module}.req.${Domain}SaveReq;
import com.lxq.train.${module}.resp.${Domain}QueryResp;
import com.lxq.train.${module}.service.${Domain}Service;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/${domain}")
public class ${Domain}AdminController {
    @Resource
    private ${Domain}Service ${domain}Service;
    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody ${Domain}SaveReq req){
        ${domain}Service.save(req);
        return new CommonResp<>();
    }

    @GetMapping("/queryList")
    public CommonResp<PageResp<${Domain}QueryResp>> queryList(@Valid ${Domain}QueryReq req){
        PageResp<${Domain}QueryResp> query = ${domain}Service.query(req);
        return new CommonResp<>(query);
    }
    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        ${domain}Service.delete(id);
        return new CommonResp<>();
    }
}
