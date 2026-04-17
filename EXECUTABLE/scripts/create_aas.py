import basyx.aas.model as model
import basyx.aas.adapter.aasx as aasx

def main():
    obj_store = model.DictObjectStore()
    
    file_store = aasx.DictSupplementaryFileContainer()

    asset_info = model.AssetInformation(
        asset_kind=model.AssetKind.INSTANCE,
        global_asset_id="https://team6.dpp/asset/battery-proto-001"
    )
    
    aas_identifier = model.Identifier('https://team6.dpp/batterypass/proto-001')
    
    aas = model.AssetAdministrationShell(
        id_=aas_identifier, 
        asset_information=asset_info, 
        id_short="Team6DPPTestShell"
    )
    obj_store.add(aas)

    submodels = []

    sm1 = model.Submodel(
        id_=model.Identifier('https://team6.dpp/sm/nameplate'), 
        id_short="DigitalNameplate"
    )
    sm1.submodel_element.add(model.Property(id_short="ManufacturerName", value_type=model.datatypes.String, value="Team 6 Energy Corp."))
    sm1.submodel_element.add(model.Property(id_short="SerialNumber", value_type=model.datatypes.String, value="T6-BATT-987654321"))
    sm1.submodel_element.add(model.Property(id_short="YearOfConstruction", value_type=model.datatypes.String, value="2026"))
    submodels.append(sm1)

    sm2 = model.Submodel(
        id_=model.Identifier('https://team6.dpp/sm/handover_docs'), 
        id_short="HandoverDocumentation"
    )
    sm2.submodel_element.add(model.Property(id_short="ManualVersion", value_type=model.datatypes.String, value="v1.2_final"))
    sm2.submodel_element.add(model.Property(id_short="SafetyInstructions", value_type=model.datatypes.String, value="Vor Nässe schützen. Nicht werfen."))
    submodels.append(sm2)

    sm3 = model.Submodel(
        id_=model.Identifier('https://team6.dpp/sm/carbon_footprint'), 
        id_short="CarbonFootprint"
    )
    sm3.submodel_element.add(model.Property(id_short="TotalCO2Equivalent_kg", value_type=model.datatypes.Double, value=145.5))
    sm3.submodel_element.add(model.Property(id_short="CalculationMethod", value_type=model.datatypes.String, value="ISO 14067"))
    submodels.append(sm3)

    sm4 = model.Submodel(
        id_=model.Identifier('https://team6.dpp/sm/technical_data'), 
        id_short="TechnicalData"
    )
    sm4.submodel_element.add(model.Property(id_short="NominalVoltage_V", value_type=model.datatypes.Double, value=400.0))
    sm4.submodel_element.add(model.Property(id_short="Capacity_kWh", value_type=model.datatypes.Double, value=82.5))
    sm4.submodel_element.add(model.Property(id_short="Weight_kg", value_type=model.datatypes.Double, value=450.0))
    submodels.append(sm4)

    sm5 = model.Submodel(
        id_=model.Identifier('https://team6.dpp/sm/product_condition'), 
        id_short="ProductCondition"
    )
    sm5.submodel_element.add(model.Property(id_short="StateOfHealth_Percent", value_type=model.datatypes.Double, value=98.2))
    sm5.submodel_element.add(model.Property(id_short="CycleCount", value_type=model.datatypes.Integer, value=42))
    submodels.append(sm5)


    sm6 = model.Submodel(
        id_=model.Identifier('https://team6.dpp/sm/material_composition'), 
        id_short="MaterialComposition"
    )
    sm6.submodel_element.add(model.Property(id_short="Lithium_Percent", value_type=model.datatypes.Double, value=8.5))
    sm6.submodel_element.add(model.Property(id_short="Cobalt_Percent", value_type=model.datatypes.Double, value=2.1))
    sm6.submodel_element.add(model.Property(id_short="ContainsCriticalRawMaterials", value_type=model.datatypes.Boolean, value=True))
    submodels.append(sm6)

    sm7 = model.Submodel(
        id_=model.Identifier('https://team6.dpp/sm/circularity'), 
        id_short="Circularity"
    )
    sm7.submodel_element.add(model.Property(id_short="RecyclabilityRate_Percent", value_type=model.datatypes.Double, value=95.0))
    sm7.submodel_element.add(model.Property(id_short="RecycledContent_Percent", value_type=model.datatypes.Double, value=15.0))
    sm7.submodel_element.add(model.Property(id_short="End_of_Life_Instruction", value_type=model.datatypes.String, value="An zertifizierten Batterie-Recycler übergeben."))
    submodels.append(sm7)

    for sm in submodels:
        obj_store.add(sm)
        aas.submodel.add(model.ModelReference(
            (model.Key(model.KeyTypes.SUBMODEL, sm.id),), 
            type_=model.Submodel
        ))

    output_file = "Team6DPPTestShell.aasx"
    
    with aasx.AASXWriter(output_file) as writer:
        writer.write_aas(
            aas_ids=[aas.id], 
            object_store=obj_store, 
            file_store=file_store
        )
        
    print(f"Erfolg! Die Datei '{output_file}' wurde mit 7 Submodellen generiert.")

if __name__ == "__main__":
    main()