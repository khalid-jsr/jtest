<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <CountryCurrency xmlns="http://www.oorsprong.org/websamples.countryinfo">
      <Boss>${boss}</Boss>
    <#list innerMap as k, v>
      <${k}><#if v??>${v}</#if></${k}>
    </#list>
    </CountryCurrency>
  </soap:Body>
</soap:Envelope>