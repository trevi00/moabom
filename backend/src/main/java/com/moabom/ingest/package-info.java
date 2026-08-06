// ingest 는 catalog 를 모른다. 이벤트(ingest :: event)만 공개하고, catalog 가 P3+ 에서 구독한다.
@org.springframework.modulith.ApplicationModule(allowedDependencies = {})
package com.moabom.ingest;
