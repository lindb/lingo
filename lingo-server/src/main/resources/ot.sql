@Metric(namespace="${serviceName}",name="rpc.server",fields={"reqs"},tags={"type","service","method"})
SELECT type,
       service,
       method,
       span.process.serviceName AS serviceName,
       trunc_sec(span.startTime, 10) AS timestamp,
       count(1) AS reqs
FROM RPCServer
GROUP BY type,
         service,
         method,
         span.process.serviceName,
         trunc_sec(span.startTime, 10);

@Metric(namespace="${serviceName}",name="rpc.client",fields={"reqs"},tags={"type","service","method"})
SELECT type,
       service,
       method,
       span.process.serviceName AS serviceName,
       trunc_sec(span.startTime, 10) AS timestamp,
       count(1) AS reqs
FROM RPCClient
GROUP BY type,
         service,
         method,
         span.process.serviceName,
         trunc_sec(span.startTime, 10);

@Metric(namespace="${serviceName}",name="database.call",fields={"reqs"},tags={"type","database","address"})
SELECT type,
       database,
       address,
       span.process.serviceName AS serviceName,
       trunc_sec(span.startTime, 10) AS timestamp,
       count(1) AS reqs
FROM DatabaseCall 
GROUP BY type,
         database,
         address,
         span.process.serviceName,
         trunc_sec(span.startTime, 10);

@Metric(namespace="${serviceName}",name="messaging.publish",fields={"reqs"},tags={"type"})
SELECT type,
       span.process.serviceName AS serviceName,
       trunc_sec(span.startTime, 10) AS timestamp,
       count(1) AS reqs
FROM MessagePublish 
GROUP BY type,
         span.process.serviceName,
         trunc_sec(span.startTime, 10);

@Metric(namespace="${serviceName}",name="messaging.receive",fields={"reqs"},tags={"type"})
SELECT type,
       span.process.serviceName AS serviceName,
       trunc_sec(span.startTime, 10) AS timestamp,
       count(1) AS reqs
FROM MessageReceive
GROUP BY type,
         span.process.serviceName,
         trunc_sec(span.startTime, 10);

@Metric(namespace="${serviceName}",name="messaging.process",fields={"reqs"},tags={"type"})
SELECT type,
       span.process.serviceName AS serviceName,
       trunc_sec(span.startTime, 10) AS timestamp,
       count(1) AS reqs
FROM MessageProcess 
GROUP BY type,
         span.process.serviceName,
         trunc_sec(span.startTime, 10);
