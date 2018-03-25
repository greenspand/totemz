package ro.cluj.totemz.screens

import ro.cluj.totemz.models.FragmentTypes

/**
 * Interface used to notify onboarding screen actions events
 * Created by sorin on 12.10.16.
 */

interface FragmentsActionsListener {
    /**
     * Notifies changes related to actions performed on the OnboardingMapIcons fragments.
     * E.g. Click listeners, swipe events
     */
    fun onNextFragment(fragType: FragmentTypes)
}