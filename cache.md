# Caching Implementation

This document outlines the caching strategy and implementation details for the Teacher Audit System.

## Overview

The application uses Spring's caching abstraction with Caffeine as the underlying cache provider. Caching is used to improve performance by reducing database load and response times for frequently accessed data.

## Cache Configuration

### Cache Manager

- **Configuration Class**: `CacheConfig.java`
- **Cache Provider**: Caffeine
- **Default TTL**: 30 minutes
- **Maximum Entries**: 1000 per cache

### Defined Caches

1. **reports**
   - Stores report data
   - Used for: All reports listing, individual report lookups

2. **reportStats**
   - Stores report statistics and aggregations
   - Used for: Dashboard metrics, summary data

3. **teacherReports**
   - Caches reports by teacher
   - Used for: Teacher-specific report listings

4. **establishmentReports**
   - Caches reports by establishment
   - Used for: Establishment-specific report listings

5. **sanctions**
   - Caches sanction-related data
   - Used for: Sanction lookups and listings

6. **referenceData**
   - Caches reference/lookup data
   - Used for: Dropdowns, enums, and other static data

## Cache Management

### Cache Monitoring

Endpoint: `GET /api/cache/stats`

Returns detailed statistics for all caches including:
- Hit/Miss counts and rates
- Load statistics
- Eviction counts
- Current size
- Configuration (maximum size)

### Cache Management

Endpoint: `GET /api/cache/clear`

Clears all caches. Use with caution in production environments.

## Implementation Details

### Cache Annotations

The application uses Spring's caching annotations:

- `@Cacheable`: Marks methods that should be cached
  ```java
  @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'all_reports'")
  public List<ReportResponseDto> getAllReportsAsDto() { ... }
  ```

- `@CacheEvict`: Marks methods that should trigger cache invalidation
  ```java
  @CacheEvict(value = CacheConfig.REPORTS_CACHE, allEntries = true)
  public Report addReport(ReportRequestDto reportDTO) { ... }
  ```

- `@Caching`: Groups multiple cache operations
  ```java
  @Caching(evict = {
      @CacheEvict(value = CacheConfig.REPORTS_CACHE, allEntries = true),
      @CacheEvict(value = CacheConfig.REPORT_STATS_CACHE, allEntries = true)
  })
  ```

### Cache Keys

Cache keys are constructed using method parameters or custom SpEL expressions. For collections, consider using a constant key since the results are the same for all users.

## Best Practices

1. **Cache Invalidation**: Always invalidate relevant caches when data is modified.
2. **Time-to-Live (TTL)**: Use appropriate TTL based on data volatility.
3. **Memory Usage**: Monitor cache sizes and adjust maximum size as needed.
4. **Selective Caching**: Only cache data that benefits from caching (frequently read, rarely changed).
5. **Cache Statistics**: Regularly monitor cache hit/miss ratios to ensure effectiveness.

## Monitoring

Monitor the following metrics:
- Cache hit rate (should be high for effective caching)
- Number of evictions
- Average load time
- Cache size

## Troubleshooting

Common issues and solutions:

1. **Stale Data**:
   - Check that `@CacheEvict` is properly configured on all modifying operations
   - Verify TTL settings

2. **High Memory Usage**:
   - Review maximum size settings
   - Check for memory leaks in cached objects

3. **Low Hit Rate**:
   - Re-evaluate what's being cached
   - Consider increasing cache size if evictions are high
