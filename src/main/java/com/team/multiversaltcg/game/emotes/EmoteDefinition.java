package com.team.multiversaltcg.game.emotes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "emote_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmoteDefinition {

    @Id
    private String id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, length = 500)
    private String gifUrl;

    private LocalDateTime updatedAt;
}
