package com.team.multiversaltcg.game.packs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pack_openings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackOpening {

    @Id
    private String id;

    @Column(nullable = false)
    private String playerId;

    private int cost;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String cardsJson;

    private LocalDateTime createdAt;
}
