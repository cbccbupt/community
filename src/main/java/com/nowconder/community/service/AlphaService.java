package com.nowconder.community.service;

import com.nowconder.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
//@Scope("phototype")
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    public AlphaService(){
        System.out.println("Instantiation AlphaService");
    }

    @PostConstruct
    //实例化之后初始化
    public void init(){
        System.out.println("Initialize AlphaService");
    }

    @PreDestroy
    //销毁前调用
    public void destroy(){
        System.out.println("Destroy AlphaService");
    }

    public String find(){
        return alphaDao.select();
    }
}
