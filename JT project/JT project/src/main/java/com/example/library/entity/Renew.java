package com.example.library.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "renewals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Renew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int daysRemaining;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private Borrow borrow;

    @Column(nullable = false)
    private boolean isFinished;
}
