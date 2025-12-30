package com.deliverycore.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Başarı/hata sonucu için generic wrapper sınıfı
 * v1.1 özelliği - Zarif hata yönetimi
 */
public sealed interface Result<T> permits Result.Success, Result.Failure {
    
    /**
     * Başarılı sonuç oluşturur
     */
    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }
    
    /**
     * Başarısız sonuç oluşturur
     */
    static <T> Result<T> failure(String error) {
        return new Failure<>(error);
    }
    
    /**
     * Sonucun başarılı olup olmadığını kontrol eder
     */
    boolean isSuccess();
    
    /**
     * Sonucun başarısız olup olmadığını kontrol eder
     */
    boolean isFailure();
    
    /**
     * Başarılı sonucun değerini döndürür
     * @throws IllegalStateException başarısız sonuç için çağrılırsa
     */
    T getValue();
    
    /**
     * Hata mesajını döndürür
     * @throws IllegalStateException başarılı sonuç için çağrılırsa
     */
    String getError();
    
    /**
     * Başarılı sonucun değerini Optional olarak döndürür
     */
    Optional<T> getValueOptional();
    
    /**
     * Hata mesajını Optional olarak döndürür
     */
    Optional<String> getErrorOptional();
    
    /**
     * Sonucu başka bir türe dönüştürür
     */
    <U> Result<U> map(Function<T, U> mapper);
    
    /**
     * Sonucu başka bir Result'a dönüştürür
     */
    <U> Result<U> flatMap(Function<T, Result<U>> mapper);
    
    /**
     * Başarılı sonuç implementasyonu
     */
    record Success<T>(T value) implements Result<T> {
        
        public Success {
            Objects.requireNonNull(value, "Success value cannot be null");
        }
        
        @Override
        public boolean isSuccess() {
            return true;
        }
        
        @Override
        public boolean isFailure() {
            return false;
        }
        
        @Override
        public T getValue() {
            return value;
        }
        
        @Override
        public String getError() {
            throw new IllegalStateException("Success result has no error");
        }
        
        @Override
        public Optional<T> getValueOptional() {
            return Optional.of(value);
        }
        
        @Override
        public Optional<String> getErrorOptional() {
            return Optional.empty();
        }
        
        @Override
        public <U> Result<U> map(Function<T, U> mapper) {
            Objects.requireNonNull(mapper, "Mapper cannot be null");
            try {
                return Result.success(mapper.apply(value));
            } catch (Exception e) {
                return Result.failure("Mapping failed: " + e.getMessage());
            }
        }
        
        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            Objects.requireNonNull(mapper, "Mapper cannot be null");
            try {
                return mapper.apply(value);
            } catch (Exception e) {
                return Result.failure("FlatMapping failed: " + e.getMessage());
            }
        }
    }
    
    /**
     * Başarısız sonuç implementasyonu
     */
    record Failure<T>(String error) implements Result<T> {
        
        public Failure {
            Objects.requireNonNull(error, "Error message cannot be null");
            if (error.trim().isEmpty()) {
                throw new IllegalArgumentException("Error message cannot be empty");
            }
        }
        
        @Override
        public boolean isSuccess() {
            return false;
        }
        
        @Override
        public boolean isFailure() {
            return true;
        }
        
        @Override
        public T getValue() {
            throw new IllegalStateException("Failure result has no value: " + error);
        }
        
        @Override
        public String getError() {
            return error;
        }
        
        @Override
        public Optional<T> getValueOptional() {
            return Optional.empty();
        }
        
        @Override
        public Optional<String> getErrorOptional() {
            return Optional.of(error);
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public <U> Result<U> map(Function<T, U> mapper) {
            return (Result<U>) this;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            return (Result<U>) this;
        }
    }
}