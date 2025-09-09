package com.example.server.model;

import java.util.Date;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "video")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String videoUrl; // URL видео на Cloudinary

    private String videoName; // название видео

    @Column(length = 1000)
    private String videoDescription; // описание видео

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = true)
    private Auth owner; // владелец видео (опционально для теста)

    @Column(nullable = false)
    private Date createdAt = new Date(); // дата загрузки
}
