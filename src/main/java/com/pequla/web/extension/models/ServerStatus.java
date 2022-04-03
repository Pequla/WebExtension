package com.pequla.web.extension.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ServerStatus {

    private PlayerStatus players;
    private WorldData world;
    private List<String> plugins;
    private String version;

}
