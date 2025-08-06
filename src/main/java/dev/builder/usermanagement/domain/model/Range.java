package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.ValueObject;
import org.jetbrains.annotations.NotNull;

public record Range(int start, int end) implements ValueObject<Range> {

    public Range{
        validate(start, end);
    }

    public int asDifference(){
        return end - start;
    }

    public boolean containsPoint(int point){
        return point >= start && point <= end;
    }

    public void validateContains(int point){
        if(!containsPoint(point)){
            throw new IllegalArgumentException("Point out of range");
        }
    }

    public int remainingOnLeft(int point){
        validateContains(point);
        return point - start;
    }

    public int remainingOnRight(int point){
        validateContains(point);
        return end - point;
    }

    public static void validate(int start, int end){
        if(!isValid(start, end)){
            throw new IllegalArgumentException("Start should be less than end");
        }
    }

    public static boolean isValid(int start, int end){
        return start <= end;
    }

    @Override
    public int compareTo(@NotNull Range range) {
        int cmp = Integer.compare(this.start, range.start);
        return cmp != 0 ? cmp : Integer.compare(this.end, range.end);
    }
}
