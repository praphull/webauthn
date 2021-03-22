run:
	@sbt run -Dhttps.port=9443 -Dhttp.port=disabled -Dconfig.file=/opt/play-confs/webauthn.conf -Dlogger.file=/opt/play-confs/webauthn-logback.xml
run-http:
	@sbt run -Dconfig.file=/opt/play-confs/webauthn.conf -Dlogger.file=/opt/play-confs/webauthn-logback.xml
