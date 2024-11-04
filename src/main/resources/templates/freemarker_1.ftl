<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <CountryCurrency xmlns="http://www.oorsprong.org/websamples.countryinfo">
      <Name>${Name}</Name>
      <DateOfBirth>${DOB?datetime}</DateOfBirth>
      <UserAge>${age}</UserAge>
    </CountryCurrency>
  </soap:Body>
</soap:Envelope>