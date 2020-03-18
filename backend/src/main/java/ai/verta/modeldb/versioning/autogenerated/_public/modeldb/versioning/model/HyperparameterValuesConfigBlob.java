// THIS FILE IS AUTO-GENERATED. DO NOT EDIT
package ai.verta.modeldb.versioning.autogenerated._public.modeldb.versioning.model;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ai.verta.modeldb.ModelDBException;
import ai.verta.modeldb.versioning.*;
import ai.verta.modeldb.versioning.blob.diff.Function3;
import ai.verta.modeldb.versioning.blob.diff.*;
import ai.verta.modeldb.versioning.blob.visitors.Visitor;
import com.pholser.junit.quickcheck.generator.*;
import com.pholser.junit.quickcheck.random.*;

public class HyperparameterValuesConfigBlob implements ProtoType {
    public Long IntValue;
    public Float FloatValue;
    public String StringValue;

    public HyperparameterValuesConfigBlob() {
        this.IntValue = 0l;
        this.FloatValue = 0.f;
        this.StringValue = "";
    }

    public Boolean isEmpty() {
        if (this.IntValue != null && !this.IntValue.equals(0l) ) {
            return false;
        }
        if (this.FloatValue != null && !this.FloatValue.equals(0.f) ) {
            return false;
        }
        if (this.StringValue != null && !this.StringValue.equals("") ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "{\"class\": \"HyperparameterValuesConfigBlob\",\"fields\": {" +
                "\"IntValue\": " + IntValue + ", " +
                "\"FloatValue\": " + FloatValue + ", " +
                "\"StringValue\": " + "\"" + StringValue + "\"" + 
                "}}";
    }

    // TODO: not consider order on lists
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof HyperparameterValuesConfigBlob)) return false;
        HyperparameterValuesConfigBlob other = (HyperparameterValuesConfigBlob) o;

        {
            Function3<Long,Long,Boolean> f = (x, y) -> x.equals(y);
            if (this.IntValue != null || other.IntValue != null) {
                if (this.IntValue == null && other.IntValue != null)
                    return false;
                if (this.IntValue != null && other.IntValue == null)
                    return false;
                if (!f.apply(this.IntValue, other.IntValue))
                    return false;
            }
        }
        {
            Function3<Float,Float,Boolean> f = (x, y) -> x.equals(y);
            if (this.FloatValue != null || other.FloatValue != null) {
                if (this.FloatValue == null && other.FloatValue != null)
                    return false;
                if (this.FloatValue != null && other.FloatValue == null)
                    return false;
                if (!f.apply(this.FloatValue, other.FloatValue))
                    return false;
            }
        }
        {
            Function3<String,String,Boolean> f = (x, y) -> x.equals(y);
            if (this.StringValue != null || other.StringValue != null) {
                if (this.StringValue == null && other.StringValue != null)
                    return false;
                if (this.StringValue != null && other.StringValue == null)
                    return false;
                if (!f.apply(this.StringValue, other.StringValue))
                    return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        this.IntValue,
        this.FloatValue,
        this.StringValue
        );
      }

    public HyperparameterValuesConfigBlob setIntValue(Long value) {
        this.IntValue = Utils.removeEmpty(value);
        return this;
    }
    public HyperparameterValuesConfigBlob setFloatValue(Float value) {
        this.FloatValue = Utils.removeEmpty(value);
        return this;
    }
    public HyperparameterValuesConfigBlob setStringValue(String value) {
        this.StringValue = Utils.removeEmpty(value);
        return this;
    }

    static public HyperparameterValuesConfigBlob fromProto(ai.verta.modeldb.versioning.HyperparameterValuesConfigBlob blob) {
        if (blob == null) {
            return null;
        }

        HyperparameterValuesConfigBlob obj = new HyperparameterValuesConfigBlob();
        {
            Function<ai.verta.modeldb.versioning.HyperparameterValuesConfigBlob,Long> f = x -> (blob.getIntValue());
            obj.IntValue = Utils.removeEmpty(f.apply(blob));
        }
        {
            Function<ai.verta.modeldb.versioning.HyperparameterValuesConfigBlob,Float> f = x -> (blob.getFloatValue());
            obj.FloatValue = Utils.removeEmpty(f.apply(blob));
        }
        {
            Function<ai.verta.modeldb.versioning.HyperparameterValuesConfigBlob,String> f = x -> (blob.getStringValue());
            obj.StringValue = Utils.removeEmpty(f.apply(blob));
        }
        return obj;
    }

    public ai.verta.modeldb.versioning.HyperparameterValuesConfigBlob.Builder toProto() {
        ai.verta.modeldb.versioning.HyperparameterValuesConfigBlob.Builder builder = ai.verta.modeldb.versioning.HyperparameterValuesConfigBlob.newBuilder();
        {
            if (this.IntValue != null && !this.IntValue.equals(0l) ) {
                Function<ai.verta.modeldb.versioning.HyperparameterValuesConfigBlob.Builder,Void> f = x -> { builder.setIntValue(this.IntValue); return null; };
                f.apply(builder);
            }
        }
        {
            if (this.FloatValue != null && !this.FloatValue.equals(0.f) ) {
                Function<ai.verta.modeldb.versioning.HyperparameterValuesConfigBlob.Builder,Void> f = x -> { builder.setFloatValue(this.FloatValue); return null; };
                f.apply(builder);
            }
        }
        {
            if (this.StringValue != null && !this.StringValue.equals("") ) {
                Function<ai.verta.modeldb.versioning.HyperparameterValuesConfigBlob.Builder,Void> f = x -> { builder.setStringValue(this.StringValue); return null; };
                f.apply(builder);
            }
        }
        return builder;
    }

    public void preVisitShallow(Visitor visitor) throws ModelDBException {
        visitor.preVisitHyperparameterValuesConfigBlob(this);
    }

    public void preVisitDeep(Visitor visitor) throws ModelDBException {
        this.preVisitShallow(visitor);
        visitor.preVisitDeepLong(this.IntValue);
        visitor.preVisitDeepFloat(this.FloatValue);
        visitor.preVisitDeepString(this.StringValue);
    }

    public HyperparameterValuesConfigBlob postVisitShallow(Visitor visitor) throws ModelDBException {
        return visitor.postVisitHyperparameterValuesConfigBlob(this);
    }

    public HyperparameterValuesConfigBlob postVisitDeep(Visitor visitor) throws ModelDBException {
        this.setIntValue(visitor.postVisitDeepLong(this.IntValue));
        this.setFloatValue(visitor.postVisitDeepFloat(this.FloatValue));
        this.setStringValue(visitor.postVisitDeepString(this.StringValue));
        return this.postVisitShallow(visitor);
    }
}