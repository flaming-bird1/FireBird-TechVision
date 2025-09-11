package com.roc.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 20)
    private String password;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "role", length = 20)
    private String role;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "blog_url", length = 255)
    private String blogUrl;

    @Column(name = "gitcode_url", length = 255)
    private String gitcodeUrl;

    @Column(name = "birthday")
    @Temporal(TemporalType.DATE)
    private Date birthday;

    @Column(name = "gender", length = 2)
    private String gender;

    @Column(name = "address", length = 100)
    private String address;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
}
