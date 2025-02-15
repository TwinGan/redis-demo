package com.example.redisdemo.Entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
@Data // Lombok 自动生成 getter/setter/toString
public class Customer implements Serializable {

    private static final long serialVersionUID = -1026724609521949715L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 自增主键
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, length = 100)
    private String email;

    @Column(unique = true, nullable = false, length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(name = "created_at", updatable = false)
    private String createdAt = LocalDateTime.now().toString();
}
