package com.project.yomozomo.domain;

import com.project.yomozomo.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "rental")
@Getter
@Setter
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_rental")
    @SequenceGenerator(name = "seq_rental", sequenceName = "seq_rental", allocationSize = 1)
    @Column(name = "rental_id")
    private Long rentalId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Temporal(TemporalType.DATE)
    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "예약";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;
}
