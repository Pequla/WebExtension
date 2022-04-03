package com.pequla.web.extension.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class PlayerStatus {

    private int max;
    private int online;
    private Set<PlayerData> list;
}
