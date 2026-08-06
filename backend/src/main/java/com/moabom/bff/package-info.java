// sink. 역방향(도메인 → bff) 의존은 verify() 가 잡는다.
@org.springframework.modulith.ApplicationModule(allowedDependencies = {
    "catalog", "playback", "profile", "recommendation", "experiment" })
package com.moabom.bff;
