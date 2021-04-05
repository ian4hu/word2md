package com.github.ian4hu.word2md;

import com.github.ian4hu.word2md.heading.Heading2HTagHandler;
import com.github.ian4hu.word2md.heading.HeadingControls;
import org.apache.commons.lang3.ClassPathUtils;
import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.TextUtils;
import org.docx4j.convert.out.ConversionFeatures;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.html.SdtToListSdtTagHandler;
import org.docx4j.convert.out.html.SdtWriter;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello World!");
        File f = new File(Main.class.getResource("/sample.docx").toURI());
        WordprocessingMLPackage wmlPackage = Docx4J.load(f);
        PrintWriter w = new PrintWriter(System.out, true);
        TextUtils.extractText(wmlPackage.getMainDocumentPart().getJaxbElement(), w);
        w.flush();
        HTMLSettings settings = new HTMLSettings();
        HeadingControls.process(wmlPackage);
        settings.setOpcPackage(wmlPackage);
        Docx4jProperties.setProperty("docx4j.Convert.Out.HTML.OutputMethodXML", true);
        SdtWriter.registerTagHandler("HTML_ELEMENT", new SdtToListSdtTagHandler());
        SdtWriter.registerTagHandler("OUTLINE_LEVEL", new Heading2HTagHandler());
        settings.getFeatures().remove(ConversionFeatures.PP_HTML_COLLECT_LISTS);


        settings.setImageDirPath(f.getPath() + "_images");
        settings.setImageTargetUri(f.getName() + "_images/");
        File out = new File(f.getParentFile(), f.getName() + ".html");
        out.createNewFile();
        try (FileOutputStream outputStream = new FileOutputStream(out)) {
            Docx4J.toHTML(settings, outputStream, Docx4J.FLAG_EXPORT_PREFER_XSL);
        }
    }
}
