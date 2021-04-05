package com.github.ian4hu.word2md.heading;

import org.docx4j.TraversalUtil;
import org.docx4j.convert.out.html.ListsToContentControls;
import org.docx4j.convert.out.html.SdtTagHandler;
import org.docx4j.finders.SdtFinder;
import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HeadingControls {
    public static Logger log = LoggerFactory.getLogger(ListsToContentControls.class);
    private final MainDocumentPart mainDocument;
    private final PropertyResolver propertyResolver;

    private HeadingControls(WordprocessingMLPackage wmlPackage) {
        mainDocument = wmlPackage.getMainDocumentPart();
        propertyResolver = wmlPackage.getMainDocumentPart().getPropertyResolver();
    }

    public static void process(WordprocessingMLPackage wmlPackage) {
        new HeadingControls(wmlPackage).process();
    }

    private void process() {
        {
            SdtFinder sdtFinder = new SdtFinder();
            TraversalUtil.visit(mainDocument.getContent(), sdtFinder);
            for (SdtElement sdtElement : sdtFinder.getSdtList()) {
                processContent(sdtElement.getSdtContent().getContent());
            }
        }

        {
            processContent(mainDocument.getContent());
        }
    }

    private void processContent(List<Object> content) {
        boolean changed = false;
        List<Object> result = new ArrayList<>(content.size());

        for (Object o : content) {
            Object unwrapped = o;
            if (o instanceof JAXBElement) {
                unwrapped = ((JAXBElement<?>) o).getValue();
            }

            if (unwrapped instanceof P) {
                P p = (P) unwrapped;
                PPr pPr = propertyResolver.getEffectivePPr(p.getPPr());
                Optional<Object> processed = Optional.ofNullable(pPr)
                        .map(PPrBase::getOutlineLvl)
                        .map(PPrBase.OutlineLvl::getVal)
                        .map(BigInteger::intValue)
                        .map(lvl -> wrapIntoSdtTag(p, lvl));
                changed = processed.isPresent() || changed;
                unwrapped = processed.orElse(unwrapped);
            }
            result.add(unwrapped);
        }

        if (changed) {
            content.clear();
            content.addAll(result);
        }
    }

    private Object wrapIntoSdtTag(P p, int outlineLvl) {
        SdtPr sdtPr = new SdtPr();
        Tag tag = new Tag();
        sdtPr.setTag(tag);
        tag.setVal("OUTLINE_LEVEL=" + (outlineLvl + 1));

        SdtBlock sdtBlock = new SdtBlock();
        sdtBlock.setSdtPr(sdtPr);

        SdtContentBlock content = new SdtContentBlock();
        sdtBlock.setSdtContent(content);

        content.getContent().add(p);
        return sdtBlock;
    }
}
