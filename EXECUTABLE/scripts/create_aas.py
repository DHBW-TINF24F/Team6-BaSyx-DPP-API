import basyx.aas.model as model
import basyx.aas.adapter.aasx as aasx

def main():
    # 1. Container initialisieren
    obj_store = model.DictObjectStore()
    file_store = aasx.DictSupplementaryFileContainer()

    # 2. Administrative Informationen (Header)
    # ExternalReference hat in v3+ KEINEN type_ Parameter mehr
    creator_ref = model.ExternalReference(
        (model.Key(model.KeyTypes.GLOBAL_REFERENCE, "https://team6.dpp/org/team6"),)
    )
    admin_info = model.AdministrativeInformation(
        version="1",
        revision="0",
        creator=creator_ref
    )

    # 3. Asset und AAS
    asset_info = model.AssetInformation(
        asset_kind=model.AssetKind.INSTANCE,
        global_asset_id="https://team6.dpp/asset/battery-proto-001"
    )
    
    aas_identifier = model.Identifier('https://team6.dpp/batterypass/proto-001')
    aas = model.AssetAdministrationShell(
        id_=aas_identifier, 
        asset_information=asset_info, 
        id_short="Team6DPPTestShell",
        administration=admin_info
    )
    obj_store.add(aas)

    submodels = []

    # =========================================================================
    # SM 1: Digital Nameplate
    # =========================================================================
    sm1 = model.Submodel(id_=model.Identifier('https://team6.dpp/sm/nameplate'), id_short="DigitalNameplate")
    
    sm1.submodel_element.add(model.Property(id_short="ManufacturerName", value_type=model.datatypes.String, value="Team 6 Energy Corp."))
    sm1.submodel_element.add(model.Property(id_short="ManufacturerProductDesignation", value_type=model.datatypes.String, value="T6-Proton-LFP"))
    
    contact_smc = model.SubmodelElementCollection(id_short="ContactInformation")
    contact_smc.value.add(model.Property(id_short="Street", value_type=model.datatypes.String, value="Innovation Drive 100"))
    contact_smc.value.add(model.Property(id_short="ZipCode", value_type=model.datatypes.String, value="70173"))
    contact_smc.value.add(model.Property(id_short="City", value_type=model.datatypes.String, value="Stuttgart"))
    contact_smc.value.add(model.Property(id_short="Country", value_type=model.datatypes.String, value="Germany"))
    sm1.submodel_element.add(contact_smc)

    sm1.submodel_element.add(model.Property(id_short="ManufacturerProductFamily", value_type=model.datatypes.String, value="Proton Series"))
    sm1.submodel_element.add(model.Property(id_short="SerialNumber", value_type=model.datatypes.String, value="T6-BATT-987654321"))
    sm1.submodel_element.add(model.Property(id_short="YearOfConstruction", value_type=model.datatypes.String, value="2026"))

    markings_sml = model.SubmodelElementList(id_short="Markings", type_value_list_element=model.SubmodelElementCollection)
    marking_ce = model.SubmodelElementCollection(id_short=None)
    marking_ce.value.add(model.Property(id_short="MarkingName", value_type=model.datatypes.String, value="CE"))
    marking_ce.value.add(model.File(id_short="MarkingFile", content_type="image/svg+xml", value="/markings/ce_logo.svg"))
    markings_sml.value.add(marking_ce)
    sm1.submodel_element.add(markings_sml)

    asset_spec_smc = model.SubmodelElementCollection(id_short="AssetSpecificProperties")
    asset_spec_smc.value.add(model.Property(id_short="GuidelineForExemption", value_type=model.datatypes.String, value="None applied"))
    sm1.submodel_element.add(asset_spec_smc)

    submodels.append(sm1)

    # =========================================================================
    # SM 2: Handover Documentation
    # =========================================================================
    sm2 = model.Submodel(id_=model.Identifier('https://team6.dpp/sm/handover_documentation'), id_short="HandoverDocumentation")
    
    sm2.submodel_element.add(model.Property(id_short="Manufacturer", value_type=model.datatypes.String, value="Team 6 Energy Corp."))
    sm2.submodel_element.add(model.Property(id_short="Customer", value_type=model.datatypes.String, value="Global Logistics SE"))
    sm2.submodel_element.add(model.Property(id_short="ContactInformation", value_type=model.datatypes.String, value="support@team6.energy"))
    sm2.submodel_element.add(model.Property(id_short="OrderReference", value_type=model.datatypes.String, value="PO-998877"))
    sm2.submodel_element.add(model.Property(id_short="ProductInformation", value_type=model.datatypes.String, value="High-Performance LFP Battery Pack"))

    handover_main_smc = model.SubmodelElementCollection(id_short="HandoverDocumentation")

    def add_instruction(parent, folder, list_id, file_path):
        smc = model.SubmodelElementCollection(id_short=folder)
        sml = model.SubmodelElementList(id_short=list_id, type_value_list_element=model.File)
        sml.value.add(model.File(id_short=None, content_type="application/pdf", value=file_path))
        smc.value.add(sml)
        parent.value.add(smc)

    add_instruction(handover_main_smc, "AssemblyInstructions", "AssemblyInstruction", "/docs/assembly.pdf")
    add_instruction(handover_main_smc, "OperatingInstructions", "OperatingInstruction", "/docs/manual.pdf")
    add_instruction(handover_main_smc, "MaintenanceInstructions", "MaintenanceInstruction", "/docs/service.pdf")
    add_instruction(handover_main_smc, "SafetyInstructions", "SafetyInstruction", "/docs/safety.pdf")

    sm2.submodel_element.add(handover_main_smc)
    submodels.append(sm2)

    # =========================================================================
    # SM 3: Carbon Footprint
    # =========================================================================
    sm3 = model.Submodel(id_=model.Identifier('https://team6.dpp/sm/carbon_footprint'), id_short="CarbonFootprint")
    pcf = model.SubmodelElementCollection(id_short="ProductCarbonFootprint")
    pcf.value.add(model.Property(id_short="DeclaringUnit", value_type=model.datatypes.String, value="kg CO2e / kWh"))
    pcf.value.add(model.Property(id_short="CarbonFootprintLifeCycleStages", value_type=model.datatypes.String, value="Cradle-to-grave"))
    pcf.value.add(model.Property(id_short="TotalCO2Equivalent", value_type=model.datatypes.Double, value=145.5))
    pcf.value.add(model.Property(id_short="PreExtractionAndProcessingCO2Equivalent", value_type=model.datatypes.Double, value=40.2))
    pcf.value.add(model.Property(id_short="MainProductionCO2Equivalent", value_type=model.datatypes.Double, value=85.3))
    pcf.value.add(model.Property(id_short="BatteryDistributionCO2Equivalent", value_type=model.datatypes.Double, value=10.5))
    pcf.value.add(model.Property(id_short="EndOfLifeAndRecyclingCO2Equivalent", value_type=model.datatypes.Double, value=9.5))
    pcf.value.add(model.Property(id_short="CarbonFootprintTotalReference", value_type=model.datatypes.String, value="https://standards.org/pcf/12345"))
    sm3.submodel_element.add(pcf)
    submodels.append(sm3)

    # =========================================================================
    # SM 4: Technical Data
    # =========================================================================
    sm4 = model.Submodel(id_=model.Identifier('https://team6.dpp/sm/technical_data'), id_short="TechnicalData")
    
    gen_info = model.SubmodelElementCollection(id_short="GeneralInformation")
    gen_info.value.add(model.Property(id_short="ManufacturerName", value_type=model.datatypes.String, value="Team 6 Energy Corp."))
    gen_info.value.add(model.File(id_short="ManufacturerLogo", content_type="image/png", value="/logos/logo_team6.png"))
    gen_info.value.add(model.Property(id_short="ManufacturerProductDesignation", value_type=model.datatypes.String, value="T6-Proton Battery"))
    gen_info.value.add(model.Property(id_short="ManufacturerArticleNumber", value_type=model.datatypes.String, value="ART-998877"))
    gen_info.value.add(model.Property(id_short="ManufacturerOrderCode", value_type=model.datatypes.String, value="ORD-12345"))
    gen_info.value.add(model.Property(id_short="ManufacturerIdentifier", value_type=model.datatypes.String, value="ID-T6-BATT"))
    gen_info.value.add(model.Property(id_short="WarrantyPeriod", value_type=model.datatypes.String, value="5 Years"))
    gen_info.value.add(model.Property(id_short="BatteryCategory", value_type=model.datatypes.String, value="LFP"))
    gen_info.value.add(model.Property(id_short="BatteryMass", value_type=model.datatypes.Double, value=450.0))
    
    prod_images = model.SubmodelElementList(id_short="ProductImages", type_value_list_element=model.File)
    prod_images.value.add(model.File(id_short=None, content_type="image/jpeg", value="/images/battery_front.jpg"))
    gen_info.value.add(prod_images)
    sm4.submodel_element.add(gen_info)

    tech_props = model.SubmodelElementCollection(id_short="TechnicalProperties")

    cev = model.SubmodelElementCollection(id_short="CapacityEnergyVoltage")
    cev.value.add(model.Property(id_short="NominalVoltage", value_type=model.datatypes.Double, value=400.0))
    cev.value.add(model.Property(id_short="MaxVoltage", value_type=model.datatypes.Double, value=420.0))
    cev.value.add(model.Property(id_short="MinVoltage", value_type=model.datatypes.Double, value=350.0))
    cev.value.add(model.Property(id_short="RatedCapacity", value_type=model.datatypes.Double, value=150.0))
    cev.value.add(model.Property(id_short="CapacityFade", value_type=model.datatypes.Double, value=2.5))
    cev.value.add(model.Property(id_short="CertifiedUsableBatteryEnergy", value_type=model.datatypes.Double, value=145.0))
    tech_props.value.add(cev)

    rtee = model.SubmodelElementCollection(id_short="RoundTripEnergyEfficiency")
    rtee.value.add(model.Property(id_short="InitialRoundTripEnergyEfficiency", value_type=model.datatypes.Integer, value=95))
    rtee.value.add(model.Property(id_short="RoundTripEnergyEfficiencyAt50ofCycleLife", value_type=model.datatypes.Integer, value=92))
    rtee.value.add(model.Property(id_short="EnergyRoundTripEfficiencyFade", value_type=model.datatypes.Double, value=3.0))
    rtee.value.add(model.Property(id_short="InitialSelfDischargingRate", value_type=model.datatypes.Integer, value=1))
    tech_props.value.add(rtee)

    res = model.SubmodelElementCollection(id_short="Resistance")
    res.value.add(model.Property(id_short="InitialInternalResistanceOnBatteryCellLevel", value_type=model.datatypes.Double, value=0.015))
    res.value.add(model.Property(id_short="InitialInternalResistanceOnBatteryPackLevel", value_type=model.datatypes.Double, value=0.150))
    res.value.add(model.Property(id_short="InitialInternalResistanceOnBatteryModuleLevel", value_type=model.datatypes.Double, value=0.050))
    res.value.add(model.Property(id_short="InternalResistanceIncreaseOfCellLevel", value_type=model.datatypes.Double, value=0.002))
    res.value.add(model.Property(id_short="InternalResistanceIncreaseOfPackLevel", value_type=model.datatypes.Double, value=0.020))
    res.value.add(model.Property(id_short="InternalResistanceIncreaseOfModulLevel", value_type=model.datatypes.Double, value=0.010))
    tech_props.value.add(res)

    pc = model.SubmodelElementCollection(id_short="PowerCapability")
    pc.value.add(model.Property(id_short="MaximumPermittedBatteryPower", value_type=model.datatypes.Double, value=350.0))
    pc.value.add(model.Property(id_short="PowerFade", value_type=model.datatypes.Double, value=5.0))
    pc.value.add(model.Property(id_short="RatioNorminalBatteryPowerAndBatteryEnergy", value_type=model.datatypes.Double, value=2.33))
    pc.value.add(model.Property(id_short="OriginalPowerCapability", value_type=model.datatypes.Double, value=360.0))
    tech_props.value.add(pc)

    temp = model.SubmodelElementCollection(id_short="Temperature")
    temp.value.add(model.Property(id_short="TemperatureRangeIdleState_LowerBoundary", value_type=model.datatypes.Double, value=-20.0))
    temp.value.add(model.Property(id_short="TemperatureRangeIdleState_UpperBoundary", value_type=model.datatypes.Double, value=55.0))
    tech_props.value.add(temp)

    life = model.SubmodelElementCollection(id_short="Lifetime")
    life.value.add(model.Property(id_short="ExpectedLifetimeInCalendarYears", value_type=model.datatypes.String, value="15 Years"))
    life.value.add(model.Property(id_short="NumberOfChargeDischargeCycles", value_type=model.datatypes.String, value="3000 Cycles"))
    life.value.add(model.Property(id_short="CapacityThresholdExhaustion", value_type=model.datatypes.Double, value=80.0))
    life.value.add(model.Property(id_short="CrateOfRelevantCycleLifeTest", value_type=model.datatypes.String, value="1C/1C"))
    tech_props.value.add(life)

    sm4.submodel_element.add(tech_props)
    submodels.append(sm4)

    # =========================================================================
    # SM 5: Product Condition
    # =========================================================================
    sm5 = model.Submodel(id_=model.Identifier('https://team6.dpp/sm/product_condition'), id_short="ProductCondition")
    
    condition_smc = model.SubmodelElementCollection(id_short="Condition")
    soh_smc = model.SubmodelElementCollection(id_short="StateOfHealth")
    soh_smc.value.add(model.Property(id_short="StateOfHealthPercent", value_type=model.datatypes.Double, value=98.5))
    soh_smc.value.add(model.Property(id_short="StateOfHealthEnergy", value_type=model.datatypes.Double, value=97.8))
    soh_smc.value.add(model.Property(id_short="StateOfHealthPower", value_type=model.datatypes.Double, value=99.0))
    condition_smc.value.add(soh_smc)
    condition_smc.value.add(model.Property(id_short="StateOfCharge", value_type=model.datatypes.Double, value=0.85))
    condition_smc.value.add(model.Property(id_short="Status", value_type=model.datatypes.String, value="Original"))
    sm5.submodel_element.add(condition_smc)

    operation_smc = model.SubmodelElementCollection(id_short="Operation")
    operation_smc.value.add(model.Property(id_short="CycleCount", value_type=model.datatypes.Integer, value=124))
    operation_smc.value.add(model.Property(id_short="TotalDischarge", value_type=model.datatypes.Double, value=4500.0))
    operation_smc.value.add(model.Property(id_short="DeepDischargeEvents", value_type=model.datatypes.Integer, value=2))
    operation_smc.value.add(model.Property(id_short="TimeSinceLastCharge", value_type=model.datatypes.Double, value=12.5))
    operation_smc.value.add(model.Property(id_short="OperatingHours", value_type=model.datatypes.Double, value=1250.0))
    
    temp_hist_smc = model.SubmodelElementCollection(id_short="TemperatureHistory")
    temp_hist_smc.value.add(model.Property(id_short="MinTemperature", value_type=model.datatypes.Double, value=-5.2))
    temp_hist_smc.value.add(model.Property(id_short="MaxTemperature", value_type=model.datatypes.Double, value=42.8))
    temp_hist_smc.value.add(model.Property(id_short="AvgTemperature", value_type=model.datatypes.Double, value=22.5))
    operation_smc.value.add(temp_hist_smc)
    
    sm5.submodel_element.add(operation_smc)
    submodels.append(sm5)

    # =========================================================================
    # SM 6: Material Composition
    # =========================================================================
    sm6 = model.Submodel(id_=model.Identifier('https://team6.dpp/sm/material_composition'), id_short="MaterialComposition")
    mat_comp_smc = model.SubmodelElementCollection(id_short="MaterialComposition")
    mat_comp_smc.value.add(model.Property(id_short="BatteryChemistry", value_type=model.datatypes.String, value="Lithium Iron Phosphate (LFP)"))
    
    crm_smc = model.SubmodelElementCollection(id_short="CriticalRawMaterials")
    crm_smc.value.add(model.Property(id_short="Cobalt", value_type=model.datatypes.Double, value=0.0))
    crm_smc.value.add(model.Property(id_short="NaturalGraphite", value_type=model.datatypes.Double, value=15.5))
    crm_smc.value.add(model.Property(id_short="Lithium", value_type=model.datatypes.Double, value=8.2))
    crm_smc.value.add(model.Property(id_short="Nickel", value_type=model.datatypes.Double, value=0.0))
    mat_comp_smc.value.add(crm_smc)

    substances_sml = model.SubmodelElementList(id_short="SubstancesList", type_value_list_element=model.SubmodelElementCollection)
    
    substance_1 = model.SubmodelElementCollection(id_short=None)
    substance_1.value.add(model.Property(id_short="SubstanceName", value_type=model.datatypes.String, value="Lithium Iron Phosphate"))
    substance_1.value.add(model.Property(id_short="SubstanceWeight", value_type=model.datatypes.Double, value=120.5))
    substance_1.value.add(model.Property(id_short="SubstancePercentage", value_type=model.datatypes.Double, value=32.0))
    substance_1.value.add(model.Property(id_short="SubstanceCASNumber", value_type=model.datatypes.String, value="15365-14-7"))
    substances_sml.value.add(substance_1)
    
    substance_2 = model.SubmodelElementCollection(id_short=None)
    substance_2.value.add(model.Property(id_short="SubstanceName", value_type=model.datatypes.String, value="Graphite"))
    substance_2.value.add(model.Property(id_short="SubstanceWeight", value_type=model.datatypes.Double, value=85.0))
    substance_2.value.add(model.Property(id_short="SubstancePercentage", value_type=model.datatypes.Double, value=22.5))
    substance_2.value.add(model.Property(id_short="SubstanceCASNumber", value_type=model.datatypes.String, value="7782-42-5"))
    substances_sml.value.add(substance_2)

    mat_comp_smc.value.add(substances_sml)
    sm6.submodel_element.add(mat_comp_smc)
    submodels.append(sm6)

    # =========================================================================
    # SM 7: Circularity
    # =========================================================================
    sm7 = model.Submodel(id_=model.Identifier('https://team6.dpp/sm/circularity'), id_short="Circularity")
    circ_smc = model.SubmodelElementCollection(id_short="Circularity")
    
    dism_smc = model.SubmodelElementCollection(id_short="Dismantling")
    dism_list = model.SubmodelElementList(id_short="DismantlingStep", type_value_list_element=model.File)
    dism_list.value.add(model.File(id_short=None, content_type="application/pdf", value="/docs/dismantling_guide.pdf"))
    dism_smc.value.add(dism_list)
    circ_smc.value.add(dism_smc)

    recycl_smc = model.SubmodelElementCollection(id_short="Recyclability")
    recycl_smc.value.add(model.Property(id_short="RecyclabilityRate", value_type=model.datatypes.Double, value=95.0))
    circ_smc.value.add(recycl_smc)

    recyc_cont_smc = model.SubmodelElementCollection(id_short="RecycledContent")
    recyc_list = model.SubmodelElementList(id_short="RecycledContentList", type_value_list_element=model.SubmodelElementCollection)
    
    item_1 = model.SubmodelElementCollection(id_short=None)
    item_1.value.add(model.Property(id_short="SubstanceName", value_type=model.datatypes.String, value="Lithium"))
    item_1.value.add(model.Property(id_short="RecycledContentPercentage", value_type=model.datatypes.Double, value=12.5))
    recyc_list.value.add(item_1)
    
    recyc_cont_smc.value.add(recyc_list)
    
    circ_smc.value.add(recyc_cont_smc)
    sm7.submodel_element.add(circ_smc)
    submodels.append(sm7)

    # =========================================================================
    # 4. Verknüpfen & Speichern
    # =========================================================================
    for sm in submodels:
        obj_store.add(sm)
        # FIX: ModelReference benötigt in v3+ den type_ Parameter ZWINGEND
        aas.submodel.add(model.ModelReference(
            (model.Key(model.KeyTypes.SUBMODEL, sm.id),), 
            type_=model.Submodel
        ))

    output_file = "Team6DPPTestShell.aasx"
    with aasx.AASXWriter(output_file) as writer:
        writer.write_aas(aas_ids=[aas.id], object_store=obj_store, file_store=file_store)
        
    print(f"Erfolg! Die Master-Shell '{output_file}' wurde fehlerfrei generiert.")

if __name__ == "__main__":
    main()