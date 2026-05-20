package com.hz.models.database;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;

@Entity
@Table(name="EVENT")
@AllArgsConstructor
public class EventSummary extends EventBase {
}
