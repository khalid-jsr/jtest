package me.test;

import java.io.*;
import java.util.*;

import freemarker.template.*;
import freemarker.core.ParseException;


public class FreeMarkerTest {
    public static void test() {
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
}
