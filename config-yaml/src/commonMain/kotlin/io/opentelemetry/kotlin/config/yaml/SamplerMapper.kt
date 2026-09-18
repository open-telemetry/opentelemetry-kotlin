package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.config.schema.model.ParentBasedSampler
import io.opentelemetry.kotlin.config.schema.model.Sampler

/**
 * The longest chain of nested `parent_based` samplers this mapper will follow.
 *
 * A `parent_based` sampler delegates to five child samplers, and each of those can be another
 * `parent_based` sampler, so the schema allows a config file to nest them without end. Mapping a
 * file like that would recurse until the stack ran out, and nesting this deep says nothing a
 * shallower config could not, so anything deeper is treated as a configuration error.
 */
internal const val MAX_PARENT_BASED_DEPTH = 10

/**
 * Maps the `tracer_provider.sampler` section of a declarative config file onto the behavior it
 * supplies. Anything the file omits, or sets to a value the spec disallows, is left unset.
 *
 * If multiple conflicting samplers are configured, this evaluates to unset (`null`). Nesting
 * `parent_based` samplers too deeply does the same: the sampler is left unset as a whole rather
 * than partly honored, so the SDK falls back to its default instead of using a sampler the file
 * never described.
 */
@ExperimentalApi
fun Sampler.toBehavior(): SamplerBehavior? {
    if (nestsDeeperThan(MAX_PARENT_BASED_DEPTH)) {
        return null
    }

    val mapped = listOfNotNull(
        alwaysOn?.let { SamplerBehavior.AlwaysOn },
        alwaysOff?.let { SamplerBehavior.AlwaysOff },
        parentBased?.toBehavior()
    )

    return mapped.singleOrNull()
}

/**
 * Whether this sampler nests `parent_based` samplers more than [limit] levels deep.
 *
 * Depth is checked before anything is mapped, rather than while mapping, because a child left
 * unset means "use the spec default" — so there would be no way to tell a child the file omitted
 * from one the mapper gave up on. Checking first keeps the answer all or nothing.
 *
 * This walks no further than [limit] levels, so it stays within the stack on the files
 * [toBehavior] needs it to reject.
 */
private fun Sampler.nestsDeeperThan(limit: Int): Boolean {
    val nested = parentBased ?: return false
    if (limit <= 0) {
        return true
    }
    return nested.children().any { it.nestsDeeperThan(limit - 1) }
}

/** The samplers this one delegates to, skipping the ones the file left out. */
private fun ParentBasedSampler.children(): List<Sampler> = listOfNotNull(
    root,
    remoteParentSampled,
    remoteParentNotSampled,
    localParentSampled,
    localParentNotSampled
)

private fun ParentBasedSampler.toBehavior(): SamplerBehavior.ParentBased =
    SamplerBehavior.ParentBased(
        root = root?.toBehavior(),
        remoteParentSampled = remoteParentSampled?.toBehavior(),
        remoteParentNotSampled = remoteParentNotSampled?.toBehavior(),
        localParentSampled = localParentSampled?.toBehavior(),
        localParentNotSampled = localParentNotSampled?.toBehavior()
    )
