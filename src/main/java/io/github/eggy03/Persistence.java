package io.github.eggy03;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Persistence {

    public static void main(String... args){
        Quarkus.run(args);
    }
}
