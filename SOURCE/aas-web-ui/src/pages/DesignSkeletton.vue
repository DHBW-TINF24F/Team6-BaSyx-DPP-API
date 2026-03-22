<template>
    <v-container fluid class="pa-4">
        <!-- Suchleiste -->
        <v-row class="mb-6" justify="center">
            <v-col cols="12" md="6">
                <v-text-field
                    prepend-inner-icon="mdi-magnify"
                    placeholder="Suchen..."
                    variant="outlined"
                    density="comfortable"
                    hide-details
                    clearable
                    class="search-bar"
                ></v-text-field>
            </v-col>
        </v-row>
        <!-- Section 1: 3 Panels nebeneinander -->
        <div class="section-title text-h5 mb-3">Kategorie 1 - Drei Panels</div>
        <v-row class="mb-6">
            <v-col cols="4" v-for="n in 3" :key="'section1-' + n">
                <v-card class="panel-card" elevation="2">
                    <v-card-title class="panel-header">
                        Panel {{ n }}
                    </v-card-title>
                    <v-card-text class="panel-content">
                        <div class="image-placeholder">
                            <v-icon size="48" color="grey">mdi-image-outline</v-icon>
                            <div class="text-caption mt-2">Bild Platzhalter</div>
                        </div>
                    </v-card-text>
                </v-card>
            </v-col>
        </v-row>

        <!-- Section 2: 1 Panel -->
        <div class="section-title text-h5 mb-3">Kategorie 2 - Ein Panel</div>
        <v-row class="mb-6">
            <v-col cols="12">
                <v-card class="panel-card" elevation="2">
                    <v-card-title class="panel-header">
                        Panel 1
                    </v-card-title>
                    <v-card-text class="panel-content">
                        <div class="image-placeholder">
                            <v-icon size="48" color="grey">mdi-image-outline</v-icon>
                            <div class="text-caption mt-2">Bild Platzhalter</div>
                        </div>
                    </v-card-text>
                </v-card>
            </v-col>
        </v-row>

        <!-- Section 3: 5 Panels -->
        <div class="section-title text-h5 mb-3">Kategorie 3 - Fünf Panels</div>
        <v-row>
            <v-col cols="12" sm="6" md="2.4" v-for="n in 5" :key="'section3-' + n" class="panel-col-5">
                <v-card class="panel-card" elevation="2">
                    <v-card-title class="panel-header">
                        Panel {{ n }}
                    </v-card-title>
                    <v-card-text class="panel-content">
                        <div class="image-placeholder">
                            <v-icon size="48" color="grey">mdi-image-outline</v-icon>
                            <div class="text-caption mt-2">Bild Platzhalter</div>
                        </div>
                    </v-card-text>
                </v-card>
            </v-col>
        </v-row>
    </v-container>
</template>

<script lang="ts" setup>
    // Imports und Logik hinzufügen
</script>

<style scoped>
    .section-title {
        color: rgb(var(--v-theme-titleText));
        border-bottom: 2px solid rgb(var(--v-theme-primary));
        padding-bottom: 8px;
    }

    .panel-card {
        height: 250px;
        display: flex;
        flex-direction: column;
        background-color: rgb(var(--v-theme-card));
    }

    .panel-header {
        background-color: rgb(var(--v-theme-cardHeader));
        font-size: 1rem;
        font-weight: 500;
        flex-shrink: 0;
    }

    .panel-content {
        flex-grow: 1;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .image-placeholder {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 150px;
        border: 2px dashed rgb(var(--v-theme-divider));
        border-radius: 8px;
        background-color: rgb(var(--v-theme-detailsCard));
    }

    /* 5 Panels in einer Reihe */
    .panel-col-5 {
        flex: 0 0 20%;
        max-width: 20%;
    }
</style>
