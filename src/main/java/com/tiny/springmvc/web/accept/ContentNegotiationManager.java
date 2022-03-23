package com.tiny.springmvc.web.accept;

import com.tiny.springmvc.http.MediaType;
import com.tiny.springmvc.web.context.request.NativeWebRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;

public class ContentNegotiationManager implements ContentNegotiationStrategy, MediaTypeFileExtensionResolver {

    private final List<ContentNegotiationStrategy> strategies = new ArrayList<>();

    private final Set<MediaTypeFileExtensionResolver> resolvers = new LinkedHashSet<>();


    /**
     * Create an instance with the given list of
     * {@code ContentNegotiationStrategy} strategies each of which may also be
     * an instance of {@code MediaTypeFileExtensionResolver}.
     * @param strategies the strategies to use
     */
    public ContentNegotiationManager(ContentNegotiationStrategy... strategies) {
        this(Arrays.asList(strategies));
    }

    /**
     * A collection-based alternative to
     * {@link #ContentNegotiationManager(ContentNegotiationStrategy...)}.
     * @param strategies the strategies to use
     * @since 3.2.2
     */
    public ContentNegotiationManager(Collection<ContentNegotiationStrategy> strategies) {
        Assert.notEmpty(strategies, "At least one ContentNegotiationStrategy is expected");
        this.strategies.addAll(strategies);
        for (ContentNegotiationStrategy strategy : this.strategies) {
            if (strategy instanceof MediaTypeFileExtensionResolver) {
                this.resolvers.add((MediaTypeFileExtensionResolver) strategy);
            }
        }
    }

    /**
     * Create a default instance with a {@link HeaderContentNegotiationStrategy}.
     */
    public ContentNegotiationManager() {
        this(new HeaderContentNegotiationStrategy());
    }


    /**
     * Return the configured content negotiation strategies.
     * @since 3.2.16
     */
    public List<ContentNegotiationStrategy> getStrategies() {
        return this.strategies;
    }

    /**
     * Find a {@code ContentNegotiationStrategy} of the given type.
     * @param strategyType the strategy type
     * @return the first matching strategy, or {@code null} if none
     * @since 4.3
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends ContentNegotiationStrategy> T getStrategy(Class<T> strategyType) {
        for (ContentNegotiationStrategy strategy : getStrategies()) {
            if (strategyType.isInstance(strategy)) {
                return (T) strategy;
            }
        }
        return null;
    }

    /**
     * Register more {@code MediaTypeFileExtensionResolver} instances in addition
     * to those detected at construction.
     * @param resolvers the resolvers to add
     */
    public void addFileExtensionResolvers(MediaTypeFileExtensionResolver... resolvers) {
        Collections.addAll(this.resolvers, resolvers);
    }

    @Override
    public List<MediaType> resolveMediaTypes(NativeWebRequest request) throws Exception {
        for (ContentNegotiationStrategy strategy : this.strategies) {
            List<MediaType> mediaTypes = strategy.resolveMediaTypes(request);
            if (mediaTypes.equals(MEDIA_TYPE_ALL_LIST)) {
                continue;
            }
            return mediaTypes;
        }
        return MEDIA_TYPE_ALL_LIST;
    }

    @Override
    public List<String> resolveFileExtensions(MediaType mediaType) {
        return doResolveExtensions(resolver -> resolver.resolveFileExtensions(mediaType));
    }

    @Override
    public List<String> getAllFileExtensions() {
        return doResolveExtensions(MediaTypeFileExtensionResolver::getAllFileExtensions);
    }

    private List<String> doResolveExtensions(Function<MediaTypeFileExtensionResolver, List<String>> extractor) {
        List<String> result = null;
        for (MediaTypeFileExtensionResolver resolver : this.resolvers) {
            List<String> extensions = extractor.apply(resolver);
            if (CollectionUtils.isEmpty(extensions)) {
                continue;
            }
            result = (result != null ? result : new ArrayList<>(4));
            for (String extension : extensions) {
                if (!result.contains(extension)) {
                    result.add(extension);
                }
            }
        }
        return (result != null ? result : Collections.emptyList());
    }

    /**
     * Return all registered lookup key to media type mappings by iterating
     * {@link MediaTypeFileExtensionResolver}s.
     * @since 5.2.4
     */
    public Map<String, MediaType> getMediaTypeMappings() {
        Map<String, MediaType> result = null;
        for (MediaTypeFileExtensionResolver resolver : this.resolvers) {
            if (resolver instanceof MappingMediaTypeFileExtensionResolver) {
                Map<String, MediaType> map = ((MappingMediaTypeFileExtensionResolver) resolver).getMediaTypes();
                if (CollectionUtils.isEmpty(map)) {
                    continue;
                }
                result = (result != null ? result : new HashMap<>(4));
                result.putAll(map);
            }
        }
        return (result != null ? result : Collections.emptyMap());
    }

}
