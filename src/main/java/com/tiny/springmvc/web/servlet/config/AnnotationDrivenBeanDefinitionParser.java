package com.tiny.springmvc.web.servlet.config;

import com.tiny.springmvc.web.servlet.handler.ConversionServiceExposingInterceptor;
import com.tiny.springmvc.web.servlet.handler.MappedInterceptor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.lang.Nullable;
import org.w3c.dom.Element;

public class AnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext context) {
        Object source = context.extractSource(element);
        XmlReaderContext readerContext = context.getReaderContext();

        RuntimeBeanReference conversionService = getConversionService(element, source, context);

        RootBeanDefinition csInterceptorDef = new RootBeanDefinition(ConversionServiceExposingInterceptor.class);
        csInterceptorDef.setSource(source);
        csInterceptorDef.getConstructorArgumentValues().addIndexedArgumentValue(0, conversionService);
        RootBeanDefinition mappedInterceptorDef = new RootBeanDefinition(MappedInterceptor.class);
        mappedInterceptorDef.setSource(source);
        mappedInterceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        mappedInterceptorDef.getConstructorArgumentValues().addIndexedArgumentValue(0, (Object) null);
        mappedInterceptorDef.getConstructorArgumentValues().addIndexedArgumentValue(1, csInterceptorDef);
        String mappedInterceptorName = readerContext.registerWithGeneratedName(mappedInterceptorDef);


        context.registerComponent(new BeanComponentDefinition(mappedInterceptorDef, mappedInterceptorName));

        //context.popAndRegisterContainingComponent();

        return null;
    }

    private RuntimeBeanReference getConversionService(Element element, @Nullable Object source, ParserContext context) {
        RuntimeBeanReference conversionServiceRef;
        if (element.hasAttribute("conversion-service")) {
            conversionServiceRef = new RuntimeBeanReference(element.getAttribute("conversion-service"));
        }
        else {
            RootBeanDefinition conversionDef = new RootBeanDefinition(FormattingConversionServiceFactoryBean.class);
            conversionDef.setSource(source);
            conversionDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            String conversionName = context.getReaderContext().registerWithGeneratedName(conversionDef);
            context.registerComponent(new BeanComponentDefinition(conversionDef, conversionName));
            conversionServiceRef = new RuntimeBeanReference(conversionName);
        }
        return conversionServiceRef;
    }
}
