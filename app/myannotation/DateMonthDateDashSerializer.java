package myannotation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import utils.DateUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * Created by win7 on 2016/8/13.
 */

@Singleton
public class DateMonthDateDashSerializer extends JsonSerializer<Long> {
    @Inject
    DateUtils dateUtils;

    @Inject
    public DateMonthDateDashSerializer(DateUtils dateUtils) {
        this.dateUtils = dateUtils;
    }

    public DateMonthDateDashSerializer() {

    }

    @Override
    public void serialize(Long value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException, JsonProcessingException {
        if (null != dateUtils && null != value) {
            if (value == 0) jsonGenerator.writeString("");
            else jsonGenerator.writeString(dateUtils.formatToMDHMSBySecond(value));
        } else jsonGenerator.writeString("");
    }

}
