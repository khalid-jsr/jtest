package me.test;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import freemarker.template.*;
import freemarker.core.ParseException;


public class FreeMarkerTest {
    public static void test()  {
        test2();
    }

    public static void test1()
    {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_33);
        cfg.setClassForTemplateLoading(FreeMarkerTest.class, "/templates");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);

        // 2. Proccess template(s)
        // 2.1. Prepare the template input:
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("Name", "Abul Khan");
        requestParams.put("age", 25);
        requestParams.put("DOB", new Date());

        try {
            Template template = cfg.getTemplate("freemarker/freemarker_1.ftl");

            // 2.3. Generate the output
            // Write output to the console
//            Writer consoleWriter = new OutputStreamWriter(System.out);
//            template.process(requestParams, consoleWriter);

            Writer writer = new StringWriter();;
            template.process(requestParams, writer);
            String result = writer.toString();
            System.out.println("Here is the FreeMarker output =======>\n");
            System.out.println(result);

            // For the sake of example, also write output into a file:
            //Writer writer = new FileWriter(new File("output.html"));

            //template.process(requestParams, writer);
        } catch (TemplateNotFoundException | MalformedTemplateNameException | ParseException | TemplateException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
//            fileWriter.close();
        }
    }


    public static void test2()
    {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_33);
        cfg.setClassForTemplateLoading(FreeMarkerTest.class, "/templates");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);

        // 2. Proccess template(s)
        // 2.1. Prepare the template input:
        Map<String, Object> requestParams = new HashMap<String, Object>();
        Map<String, Object> paymentDtotMap = new HashMap<String, Object>();
        paymentDtotMap.put("id", 1043);
        paymentDtotMap.put("gateway", 1);
        paymentDtotMap.put("paymentGatewayActivityID", 4917731);
        paymentDtotMap.put("transactionNumber", "000000006586");
        paymentDtotMap.put("authCode", "OK7522");
        paymentDtotMap.put("reasonCode", "CM1000");
        paymentDtotMap.put("reasonDescription", null);
        paymentDtotMap.put("accountNumber", "XXXX1111");
        paymentDtotMap.put("accountType", "VISA");
        paymentDtotMap.put("paymentTypeValue", 20);
        paymentDtotMap.put("currencyCode", "USD");
        paymentDtotMap.put("transactionType", "AUTHORIZE_AND_CAPTURE");
        paymentDtotMap.put("transactionAmount", new BigDecimal("10.0000"));
        paymentDtotMap.put("transactionStatus", "SUCCESS");
        paymentDtotMap.put("transactionDate", new BigDecimal("1728986021.996195000"));

        requestParams.put("boss", "The Big Boss");
        requestParams.put("innerMap", paymentDtotMap);

        try {
            Template template = cfg.getTemplate("freemarker/freemarker_2.ftl");

            // 2.3. Generate the output
            // Write output to the console
//            Writer consoleWriter = new OutputStreamWriter(System.out);
//            template.process(requestParams, consoleWriter);

            Writer writer = new StringWriter();;
            template.process(requestParams, writer);
            String result = writer.toString();
            System.out.println("Here is the FreeMarker output =======>\n");
            System.out.println(result);

            // For the sake of example, also write output into a file:
            //Writer writer = new FileWriter(new File("output.html"));

            //template.process(requestParams, writer);
        } catch (TemplateNotFoundException | MalformedTemplateNameException | ParseException | TemplateException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
//            fileWriter.close();
        }
    }
}
