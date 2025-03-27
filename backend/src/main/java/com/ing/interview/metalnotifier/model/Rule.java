package com.ing.interview.metalnotifier.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Operator operator;
    
    private String operand;
    
    @ManyToOne
    @JoinColumn(name = "template_id")
    @JsonIgnore
    private NotificationTemplate template;
    
    public Rule() {}
    
    public Rule(Operator operator, String operand) {
        this.operator = operator;
        this.operand = operand;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public String getOperand() {
        return operand;
    }

    public void setOperand(String operand) {
        this.operand = operand;
    }

    public NotificationTemplate getTemplate() {
        return template;
    }

    public void setTemplate(NotificationTemplate template) {
        this.template = template;
    }
    
    public boolean evaluate(MetalPrice metalPrice) {
        switch (operator) {
            case ITEM_IS:
                return metalPrice.getItemType().equals(operand);
            case ITEM_IS_NOT:
                return !metalPrice.getItemType().equals(operand);
            case PRICE_IS_EQUAL_TO:
                return metalPrice.getPrice().compareTo(new BigDecimal(operand)) == 0;
            case PRICE_IS_GREATER_THAN:
                return metalPrice.getPrice().compareTo(new BigDecimal(operand)) > 0;
            case PRICE_IS_GREATER_THAN_OR_EQUAL_TO:
                return metalPrice.getPrice().compareTo(new BigDecimal(operand)) >= 0;
            case PRICE_IS_LESS_THAN:
                return metalPrice.getPrice().compareTo(new BigDecimal(operand)) < 0;
            case PRICE_IS_LESS_THAN_OR_EQUAL_TO:
                return metalPrice.getPrice().compareTo(new BigDecimal(operand)) <= 0;
            default:
                return false;
        }
    }
}

