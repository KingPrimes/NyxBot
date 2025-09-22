package com.nyx.bot.modules.warframe.utils.riven_calculation;

import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendCompute;
import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendModel;
import com.nyx.bot.modules.warframe.entity.RivenAnalyseTrend;
import com.nyx.bot.modules.warframe.entity.exprot.Weapons;

import java.util.Map;

import static com.nyx.bot.modules.warframe.entity.exprot.Weapons.ProductCategory.*;

@FunctionalInterface
public interface AttributeCalculator {
   Map<Weapons.ProductCategory, AttributeCalculator> CALCULATORS =
            Map.of(
                    Pistols, WeaponSetters::setPistols,
                    LongGuns, WeaponSetters::setLongGuns,
                    Melee,  WeaponSetters::setMelee,
                    SpaceGuns, WeaponSetters::setSpaceGuns,
                    SpaceMelee, WeaponSetters::setSpaceMelee,
                    SpecialItems, WeaponSetters::setSpecialItems,
                    CrewShipWeapons, WeaponSetters::setCrewShipWeapons,
                    SentinelWeapons, WeaponSetters::setSentinelWeapons,
                    Shotguns, WeaponSetters::setShotguns
            );

    void calculate(RivenAnalyseTrendCompute.Attribute attr,
                   RivenAnalyseTrendModel.Attribute model,
                   double omegaAttenuation,
                   RivenAnalyseTrend analyseTrend,
                   int totalAttributes);
}