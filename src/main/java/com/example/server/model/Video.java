package com.example.server.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @Column(nullable = false)
    private String videoUrl;

    @Column(nullable = false)
    private String videoName;

    @Column(length = 1000)
    private String videoDescription;

    @Column(nullable = true)
    private String videoPreview;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = true)
    private Auth owner;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date createdAt;
}
