<template>
  <v-dialog v-model="dialog" persistent width="1300">
    <v-card>
      <v-card-title>
        <span class="headline">
          {{ 'New Participation' }}
        </span>
      </v-card-title>
      <v-card-text>
        <v-form ref="form" lazy-validation>
          <v-row>
            <v-col cols="12" sm="6" md="4">
              <v-text-field
                label="Rating"
                :rules="[
                  (v) =>
                    isNumberValid(v) || 'Rating between 1 and 5 is required',
                ]"
                required
                v-model="rating"
                data-cy="ratingInput"
              ></v-text-field>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="blue-darken-1"
          variant="text"
          @click="$emit('close-participation-dialog')"
        >
          Close
        </v-btn>
        <v-btn
          color="blue-darken-1"
          variant="text"
          @click="updateParticipation"
          data-cy="saveParticipation"
        >
          Save
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script lang="ts">
import { Vue, Component, Model, Prop } from 'vue-property-decorator';
import Participation from '@/models/participation/Participation';
import RemoteServices from '@/services/RemoteServices';
import VueCtkDateTimePicker from 'vue-ctk-date-time-picker';
import 'vue-ctk-date-time-picker/dist/vue-ctk-date-time-picker.css';
import { ISOtoString } from '@/services/ConvertDateService';
import Enrollment from '@/models/enrollment/Enrollment';

Vue.component('VueCtkDateTimePicker', VueCtkDateTimePicker);
@Component({
  methods: { ISOtoString },
})
export default class ParticipationSelectionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Enrollment, required: true })
  readonly enrollment!: Enrollment;

  rating: number | null = null;

  cypressCondition: boolean = false;

  async created() {}

  isNumberValid(value: any) {
    console.log('value is', value);
    if (value == null) return true;
    if (!/^\d+$/.test(value)) return false;
    const parsedValue = parseInt(value);
    return parsedValue >= 1 && parsedValue <= 5;
  }

  get canSave(): boolean {
    return this.cypressCondition || !!this.rating;
  }

  async updateParticipation() {
    if ((this.$refs.form as Vue & { validate: () => boolean }).validate()) {
      try {
        let newParticipation = new Participation();
        newParticipation.rating = this.rating;
        newParticipation.volunteerId = this.enrollment.volunteerId;
        console.log(newParticipation);
        const result = await RemoteServices.selectParticipant(
          this.$store.getters.getActivity.id,
          newParticipation,
        );
        this.$emit('save-participation', result);
      } catch (error) {
        console.log('error', error);
        await this.$store.dispatch('error', error);
      }
    }
  }
}
</script>

<style scoped lang="scss"></style>
